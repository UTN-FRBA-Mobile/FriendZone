package com.example.friendzone.data.repository

import com.example.friendzone.data.local.CacheKeys
import com.example.friendzone.data.local.LocalCacheManager
import com.example.friendzone.data.mapper.DtoMapper
import com.example.friendzone.data.remote.api.InvitationsApi
import com.example.friendzone.data.remote.dto.CreateInvitationRequest
import com.example.friendzone.data.remote.dto.UpdateInvitationRequest
import com.example.friendzone.data.remote.safeApiCall
import com.example.friendzone.domain.model.Invitation
import com.example.friendzone.domain.model.InvitationStatus
import com.example.friendzone.domain.model.PendingInvitation
import com.example.friendzone.domain.repository.InvitationRepository
import com.example.friendzone.domain.result.ApiResult
import kotlinx.serialization.serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvitationRepositoryImpl @Inject constructor(
    private val invitationsApi: InvitationsApi,
    private val cacheManager: LocalCacheManager,
) : InvitationRepository {
    override suspend fun getCachedByEvent(eventId: String): List<Invitation>? =
        cacheManager.getList(CacheKeys.eventInvitations(eventId), serializer())

    override suspend fun getCachedMinePending(): List<PendingInvitation>? =
        cacheManager.getList(CacheKeys.PENDING_INVITATIONS, serializer())

    override suspend fun create(eventId: String, emailOrUsername: String): ApiResult<Invitation> =
        safeApiCall {
            DtoMapper.toInvitation(
                invitationsApi.create(eventId, CreateInvitationRequest(emailOrUsername)),
            )
        }.also { result ->
            if (result is ApiResult.Success) {
                invalidateInvitationCaches(eventId)
            }
        }

    override suspend fun getByEvent(eventId: String): ApiResult<List<Invitation>> {
        val result = safeApiCall {
            invitationsApi.getByEvent(eventId).map(DtoMapper::toInvitation)
        }
        return when (result) {
            is ApiResult.Success -> {
                cacheManager.putList(CacheKeys.eventInvitations(eventId), result.data, serializer())
                result
            }
            is ApiResult.Error -> getCachedByEvent(eventId)?.let { ApiResult.Success(it) } ?: result
            ApiResult.Loading -> result
        }
    }

    override suspend fun getMinePending(): ApiResult<List<PendingInvitation>> {
        val result = safeApiCall {
            invitationsApi.getMinePending().map(DtoMapper::toPendingInvitation)
        }
        return when (result) {
            is ApiResult.Success -> {
                cacheManager.putList(CacheKeys.PENDING_INVITATIONS, result.data, serializer())
                result
            }
            is ApiResult.Error -> getCachedMinePending()?.let { ApiResult.Success(it) } ?: result
            ApiResult.Loading -> result
        }
    }

    override suspend fun respond(invitationId: String, status: InvitationStatus): ApiResult<Invitation> =
        safeApiCall {
            DtoMapper.toInvitation(
                invitationsApi.respond(
                    invitationId,
                    UpdateInvitationRequest(DtoMapper.invitationStatusToApi(status)),
                ),
            )
        }.also { result ->
            if (result is ApiResult.Success) {
                cacheManager.delete(CacheKeys.PENDING_INVITATIONS)
                cacheManager.deleteByPrefix(CacheKeys.EVENT_INVITATIONS_PREFIX)
            }
        }

    private suspend fun invalidateInvitationCaches(eventId: String) {
        cacheManager.delete(CacheKeys.eventInvitations(eventId))
        cacheManager.delete(CacheKeys.PENDING_INVITATIONS)
    }
}
