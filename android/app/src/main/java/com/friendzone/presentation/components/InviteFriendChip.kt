package com.example.friendzone.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.friendzone.domain.model.User
import com.example.friendzone.domain.util.resolveApiAssetUrl
import com.example.friendzone.ui.theme.BorderGray
import com.example.friendzone.ui.theme.TextMain
import com.example.friendzone.ui.theme.Surface
import com.example.friendzone.ui.theme.Surface2

@Composable
fun InviteFriendChip(
    friend: User,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) TextMain else BorderGray
    val bg = if (selected) Surface else Surface2
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        ProfileIconItem(
            displayName = friend.displayName,
            profilePictureUrl = resolveApiAssetUrl(friend.profilePictureUrl),
            size = 36.dp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(friend.displayName, style = MaterialTheme.typography.labelMedium, color = TextMain)
    }
}
