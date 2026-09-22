package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun RoleSwitcherBar(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    onOpenHub: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onOpenHub != null) {
                IconButton(
                    onClick = onOpenHub,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "App Suite Hub",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            RoleTabItem(
                role = UserRole.CUSTOMER,
                label = "Rider App",
                icon = Icons.Default.DirectionsBike,
                isSelected = currentRole == UserRole.CUSTOMER,
                onClick = { onRoleSelected(UserRole.CUSTOMER) },
                modifier = Modifier.weight(1f)
            )
            RoleTabItem(
                role = UserRole.CAPTAIN,
                label = "Captain App",
                icon = Icons.Default.TwoWheeler,
                isSelected = currentRole == UserRole.CAPTAIN,
                onClick = { onRoleSelected(UserRole.CAPTAIN) },
                modifier = Modifier.weight(1f)
            )
            RoleTabItem(
                role = UserRole.ADMIN,
                label = "Admin Panel",
                icon = Icons.Default.AdminPanelSettings,
                isSelected = currentRole == UserRole.ADMIN,
                onClick = { onRoleSelected(UserRole.ADMIN) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RoleTabItem(
    role: UserRole,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = when (role) {
        UserRole.CUSTOMER -> VeloAmber
        UserRole.CAPTAIN -> VeloEmerald
        UserRole.ADMIN -> VeloTeal
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.22f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
