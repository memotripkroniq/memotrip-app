package com.example.memotrip_kroniq.ui.tripdetail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.theme.AppTheme

data class TripMemberUi(
    val id: String,
    val name: String,
    val avatarRes: Int? = null
)

@Composable
fun TripMembersSection(
    members: List<TripMemberUi>,
    onAddMemberClick: () -> Unit
) {
    val s = LocalUiScaler.current

    Column(
        modifier = Modifier
    ) {
        Text(
            text = stringResource(R.string.trip_detail_trip_members),
            color = Color.White,
            fontSize = 16f.fs(s),
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10f.sy(s)))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8f.sx(s))
        ) {
            items(
                items = members,
                key = { it.id }
            ) { member ->
                MemberTile(
                    name = member.name,
                    avatarRes = member.avatarRes
                )
            }

            item {
                AddMemberTile(onClick = onAddMemberClick)
            }
        }
    }
}

@Composable
private fun MemberTile(
    name: String,
    avatarRes: Int?
) {
    val s = LocalUiScaler.current
    val size = 80.dp
    val radius = 10.dp

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        if (avatarRes != null) {
            Image(
                painter = painterResource(avatarRes),
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(radius)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(radius))
                    .background(Color(0xFF2A2A2A))
            )
        }

        Spacer(Modifier.height(8f.sy(s)))

        Text(
            text = name,
            color = Color.White,
            fontSize = 16f.fs(s),
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
private fun AddMemberTile(
    onClick: () -> Unit
) {
    val s = LocalUiScaler.current

    Image(
        painter = painterResource(R.drawable.tripdetail_addnewmember),
        contentDescription = null,
        modifier = Modifier
            .size(80.dp) // držíme stejnou vizuální stopu jako ostatní tiles
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
    )
}


@Preview(
    name = "TripMembersSection",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 482,
    heightDp = 120
)
@Composable
private fun TripMembersSectionPreview() {
    AppTheme {
        TripMembersSection(
            members = listOf(
                TripMemberUi("1", "Kristin", avatarRes = R.drawable.some_avatar_kristin),
                TripMemberUi("2", "Peetr", avatarRes = R.drawable.some_avatar_peetr),
                TripMemberUi("3", "Lily", avatarRes = R.drawable.some_avatar_lily),
                TripMemberUi("4", "Sara", avatarRes = R.drawable.some_avatar_sara)
            ),
            onAddMemberClick = {}
        )
    }
}
