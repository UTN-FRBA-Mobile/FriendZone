package com.example.friendzone.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.friendzone.ui.theme.FzBorderGray
import com.example.friendzone.ui.theme.FzPrimary
import com.example.friendzone.ui.theme.FzPrimaryDark
import com.example.friendzone.ui.theme.FzPrimaryLight
import com.example.friendzone.ui.theme.FzTextMain
import com.example.friendzone.ui.theme.FzTextSecondary
import com.example.friendzone.ui.theme.FzError
import com.example.friendzone.ui.theme.FzSurface

@Composable
fun FriendZoneSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedTrackColor = FzPrimary,
            checkedThumbColor = Color.White,
            checkedBorderColor = FzPrimary,
            uncheckedTrackColor = FzBorderGray,
            uncheckedThumbColor = Color.White,
            uncheckedBorderColor = FzBorderGray,
            disabledCheckedTrackColor = FzPrimary.copy(alpha = 0.5f),
            disabledUncheckedTrackColor = FzPrimaryLight,
        ),
    )
}

@Composable
fun FriendZonePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FzPrimary,
            contentColor = Color.White,
            disabledContainerColor = FzPrimary.copy(alpha = 0.4f),
        ),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun FriendZoneOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = FzSurface,
            contentColor = FzPrimary,
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, FzBorderGray),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                icon()
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun FriendZoneTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    optionalLabel: String? = null,
    required: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = FzTextMain,
            )
            if (required) {
                Text(" *", color = FzError)
            }
            optionalLabel?.let {
                Text(
                    text = " $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = FzTextSecondary,
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        val fieldShape = RoundedCornerShape(10.dp)
        val fieldHeight = if (minLines > 1) (minLines * 24 + 26).dp else 48.dp
        if (onClick != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fieldHeight)
                    .clip(fieldShape)
                    .background(FzSurface)
                    .border(1.5.dp, FzBorderGray, fieldShape)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = value.ifEmpty { placeholder },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (value.isEmpty()) FzTextSecondary else FzTextMain,
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fieldHeight)
                    .background(FzSurface, fieldShape)
                    .border(1.5.dp, FzBorderGray, fieldShape)
                    .padding(horizontal = 14.dp, vertical = 13.dp),
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    readOnly = readOnly,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = FzTextMain),
                    singleLine = singleLine && minLines == 1,
                    minLines = minLines,
                    visualTransformation = if (isPassword && !passwordVisible) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                    decorationBox = { inner ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (value.isEmpty() && placeholder.isNotEmpty()) {
                                    Text(placeholder, color = FzTextSecondary)
                                }
                                inner()
                            }
                            if (isPassword) {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(0.dp),
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) {
                                            Icons.Filled.Visibility
                                        } else {
                                            Icons.Filled.VisibilityOff
                                        },
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = FzTextSecondary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    },
                )
            }
        }
    }
}
