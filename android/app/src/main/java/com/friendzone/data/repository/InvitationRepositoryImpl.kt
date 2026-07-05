package com.example.friendzone.data.repository

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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvitationRepositoryImpl @Inject constructor(
    private val invitationsApi: InvitationsApi,
) : InvitationRepository {
    override suspend fun create(eventId: String, emailOrUsername: String): ApiResult<Invitation> =
        safeApiCall {
            DtoMapper.toInvitation(
                invitationsApi.create(eventId, CreateInvitationRequest(emailOrUsername)),
            )
        }

    override suspend fun getByEvent(eventId: String): ApiResult<List<Invitation>> = safeApiCall {
        invitationsApi.getByEvent(eventId).map(DtoMapper::toInvitation)
    }

    override suspend fun getMinePending(): ApiResult<List<PendingInvitation>> = safeApiCall {
        invitationsApi.getMinePending().map(DtoMapper::toPendingInvitation)
    }

    override suspend fun respond(invitationId: String, status: InvitationStatus): ApiResult<Invitation> =
        safeApiCall {
            DtoMapper.toInvitation(
                invitationsApi.respond(
                    invitationId,
                    UpdateInvitationRequest(DtoMapper.invitationStatusToApi(status)),
                ),
            )
        }
}
