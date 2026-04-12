package com.example.memotrip_kroniq.ui.kroniq

import PreviewUiScaler
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.data.remote.dto.KroniqMemberDto
import com.example.memotrip_kroniq.ui.components.PhotoPickerOverlay
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.home.components.modifiers.innerTopShadow
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.example.memotrip_kroniq.ui.utils.createImageFile
import innerShadow
import kotlinx.coroutines.launch

private enum class KroniqTab {
    TIMELINE,
    THEMES,
    KRONIQ
}

private data class KroniqNode(
    val label: String,
    val centerX: Float,
    val centerY: Float,
    val dimmed: Boolean = false
)

@Composable
fun KroniqScreen(
    onBack: () -> Unit = {},
    reloadToken: Int = 0,
    onAddMemberClick: () -> Unit = {},
    onAddGuestClick: () -> Unit = {},
    onEditMemberClick: (KroniqMemberDto) -> Unit = {}
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val tokenStore = remember { TokenDataStore(context) }
    val authRepository = remember {
        AuthRepository(
            api = RetrofitClient.authApi,
            tokenStore = tokenStore
        )
    }
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(KroniqTab.KRONIQ) }
    var topImageUri by remember { mutableStateOf<Uri?>(null) }
    var topImageUrl by remember { mutableStateOf<String?>(null) }
    var members by remember { mutableStateOf<List<KroniqMemberDto>>(emptyList()) }
    var removingMemberId by remember { mutableStateOf<String?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoActionSheet by remember { mutableStateOf(false) }

    LaunchedEffect(authRepository, reloadToken) {
        runCatching { authRepository.getKroniqMe() }
            .onSuccess { me ->
                topImageUrl = me.kroniqImageUrl
                members = me.members
            }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            topImageUri = uri
            scope.launch {
                runCatching {
                    authRepository.uploadKroniqPhoto(contentResolver, uri)
                }.onSuccess { uploadedUrl ->
                    topImageUrl = uploadedUrl
                    topImageUri = null
                }
            }
        }
        showPhotoActionSheet = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            val capturedUri = tempPhotoUri
            topImageUri = capturedUri
            if (capturedUri != null) {
                scope.launch {
                    runCatching {
                        authRepository.uploadKroniqPhoto(contentResolver, capturedUri)
                    }.onSuccess { uploadedUrl ->
                        topImageUrl = uploadedUrl
                        topImageUri = null
                    }
                }
            }
        }
        showPhotoActionSheet = false
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val photoFile = createImageFile(context)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AppTopBar(
            title = stringResource(R.string.kroniq_title),
            showBack = true,
            onBackClick = onBack,
            onMenuClick = null
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopImageSlot(
                imageModel = topImageUri ?: topImageUrl,
                onClick = { showPhotoActionSheet = true },
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            KroniqTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            KroniqTreeCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 28.dp),
                members = members,
                onAddMemberClick = onAddMemberClick,
                onAddGuestClick = onAddGuestClick,
                onEditMemberClick = { member ->
                    if (removingMemberId != null) return@KroniqTreeCard

                    removingMemberId = member.id
                    scope.launch {
                        runCatching {
                            authRepository.deleteKroniqMember(member.id)
                            authRepository.getKroniqMe()
                        }.onSuccess { me ->
                            topImageUrl = me.kroniqImageUrl
                            members = me.members
                            onEditMemberClick(member)
                        }.onFailure { error ->
                            Log.e("KRONIQ_REMOVE_MEMBER", "Remove member failed for memberId=${member.id}", error)
                        }

                        removingMemberId = null
                    }
                }
            )
        }
    }

    if (showPhotoActionSheet) {
        PhotoPickerOverlay(
            canDelete = topImageUri != null || !topImageUrl.isNullOrBlank(),
            onTakePhoto = {
                if (
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val photoFile = createImageFile(context)
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
                    tempPhotoUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onPickFromGallery = { galleryLauncher.launch("image/*") },
            onDeletePhoto = {
                topImageUri = null
                scope.launch {
                    runCatching { authRepository.deleteKroniqPhoto() }
                        .onSuccess { topImageUrl = null }
                }
                showPhotoActionSheet = false
            },
            onDismiss = { showPhotoActionSheet = false }
        )
    }
}

@Composable
private fun KroniqTabs(
    selectedTab: KroniqTab,
    onTabSelected: (KroniqTab) -> Unit
) {
    val s = LocalUiScaler.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50f.sy(s))
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF383A41))
            .border(1.5.dp, Color(0xFF747781), RoundedCornerShape(10.dp))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KroniqTabButton(
            title = stringResource(R.string.kroniq_tab_timeline),
            selected = selectedTab == KroniqTab.TIMELINE,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (selectedTab == KroniqTab.TIMELINE)
                        Modifier.innerTopShadow(alpha = 0.16f, height = 20f)
                    else Modifier
                ),
            onClick = { onTabSelected(KroniqTab.TIMELINE) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        KroniqTabButton(
            title = stringResource(R.string.kroniq_tab_themes),
            selected = selectedTab == KroniqTab.THEMES,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (selectedTab == KroniqTab.THEMES)
                        Modifier.innerTopShadow(alpha = 0.16f, height = 20f)
                    else Modifier
                ),
            onClick = { onTabSelected(KroniqTab.THEMES) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        KroniqTabButton(
            title = stringResource(R.string.kroniq_tab_kroniq),
            selected = selectedTab == KroniqTab.KRONIQ,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (selectedTab == KroniqTab.KRONIQ)
                        Modifier.innerTopShadow(alpha = 0.16f, height = 20f)
                    else Modifier
                ),
            onClick = { onTabSelected(KroniqTab.KRONIQ) }
        )
    }
}

@Composable
private fun TopImageSlot(
    imageModel: Any?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (imageModel != null) {
        Box(
            modifier = modifier
                .size(100.dp)
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = stringResource(R.string.kroniq_add_member),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 12.dp, y = 12.dp)
                    .size(32.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
            ) {
                Image(
                    painter = painterResource(R.drawable.tripdetail_ic_edit),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    } else {
        Image(
            painter = painterResource(R.drawable.tripdetail_addnewmember),
            contentDescription = stringResource(R.string.kroniq_add_member),
            modifier = modifier
                .size(100.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        )
    }
}

@Composable
private fun KroniqTabButton(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val s = LocalUiScaler.current

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF0077C8) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12f.sx(s)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18f.fs(s),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun KroniqTreeCard(
    modifier: Modifier = Modifier,
    members: List<KroniqMemberDto>,
    onAddMemberClick: () -> Unit,
    onAddGuestClick: () -> Unit,
    onEditMemberClick: (KroniqMemberDto) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
    ) {
        val slotSize = 72.dp
        val mainMemberSize = slotSize
        val adminMember = members.firstOrNull { it.role.equals("ADMIN", ignoreCase = true) }
        val resolvedMembers = members
            .filterNot { it.id == adminMember?.id || it.role.equals("GUEST", ignoreCase = true) }
            .take(4)
        val resolvedGuests = members
            .filter { it.role.equals("GUEST", ignoreCase = true) }
            .take(2)
        val topNodes = listOf(
            KroniqNode(stringResource(R.string.kroniq_member), 0.16f, 0.18f),
            KroniqNode(stringResource(R.string.kroniq_member), 0.84f, 0.18f)
        )
        val middleNodes = listOf(
            KroniqNode(stringResource(R.string.kroniq_member), 0.16f, 0.40f),
            KroniqNode(stringResource(R.string.kroniq_member), 0.84f, 0.40f)
        )
        val bottomNodes = listOf(
            KroniqNode(stringResource(R.string.kroniq_guest), 0.16f, 0.86f, dimmed = true),
            KroniqNode(stringResource(R.string.kroniq_guest), 0.84f, 0.86f, dimmed = true)
        )
        val brandCenterX = 0.50f
        val brandCenterY = 0.12f
        val mainMemberCenterX = 0.50f
        val mainMemberCenterY = 0.63f

        Image(
            painter = painterResource(R.drawable.kroniq_screen_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 1f
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            val slotRadiusX = (slotSize.toPx() / 2f)
            val slotRadiusY = (slotSize.toPx() / 2f)
            val centerGap = 48.dp.toPx()
            val edgeInset = 12.dp.toPx()

            val topLeftCenter = Offset(size.width * topNodes[0].centerX, size.height * topNodes[0].centerY)
            val topRightCenter = Offset(size.width * topNodes[1].centerX, size.height * topNodes[1].centerY)
            val middleLeftCenter = Offset(size.width * middleNodes[0].centerX, size.height * middleNodes[0].centerY)
            val middleRightCenter = Offset(size.width * middleNodes[1].centerX, size.height * middleNodes[1].centerY)

            val middleLeft = Offset(middleLeftCenter.x, middleLeftCenter.y)
            val middleRight = Offset(middleRightCenter.x, middleRightCenter.y)
            val horizontalMid = Offset(
                x = (middleLeft.x + middleRight.x) / 2f,
                y = middleLeft.y
            )
            val mainMemberTop = Offset(
                x = size.width * mainMemberCenterX,
                y = size.height * (mainMemberCenterY - 0.10f)
            )

            val leftVerticalStart = Offset(topLeftCenter.x, topLeftCenter.y + slotRadiusY + edgeInset)
            val leftVerticalEnd = Offset(middleLeftCenter.x, middleLeftCenter.y - slotRadiusY - edgeInset)
            val rightVerticalStart = Offset(topRightCenter.x, topRightCenter.y + slotRadiusY + edgeInset)
            val rightVerticalEnd = Offset(middleRightCenter.x, middleRightCenter.y - slotRadiusY - edgeInset)
            val horizontalStart = Offset(middleLeftCenter.x + slotRadiusX + edgeInset, middleLeftCenter.y)
            val horizontalEnd = Offset(middleRightCenter.x - slotRadiusX - edgeInset, middleRightCenter.y)
            val leftHorizontalEnd = Offset(horizontalMid.x - (centerGap / 2f), middleLeftCenter.y)
            val rightHorizontalStart = Offset(horizontalMid.x + (centerGap / 2f), middleRightCenter.y)
            val centerVerticalStart = Offset(horizontalMid.x, middleLeftCenter.y + (centerGap / 4f))

            drawLine(
                color = Color.White.copy(alpha = 0.38f),
                start = leftVerticalStart,
                end = leftVerticalEnd,
                strokeWidth = 3f,
                pathEffect = dash
            )
            drawLine(
                color = Color.White.copy(alpha = 0.38f),
                start = rightVerticalStart,
                end = rightVerticalEnd,
                strokeWidth = 3f,
                pathEffect = dash
            )
            drawLine(
                color = Color.White.copy(alpha = 0.38f),
                start = horizontalStart,
                end = leftHorizontalEnd,
                strokeWidth = 3f,
                pathEffect = dash
            )
            drawLine(
                color = Color.White.copy(alpha = 0.38f),
                start = rightHorizontalStart,
                end = horizontalEnd,
                strokeWidth = 3f,
                pathEffect = dash
            )
            drawLine(
                color = Color.White.copy(alpha = 0.38f),
                start = centerVerticalStart,
                end = mainMemberTop,
                strokeWidth = 3f,
                pathEffect = dash
            )
        }

        topNodes.forEach { node ->
            val member = resolvedMembers.getOrNull(topNodes.indexOf(node))
            MemberColumn(
                label = member?.name?.takeIf { it.isNotBlank() } ?: node.label,
                isResolvedMember = member?.name?.isNotBlank() == true,
                dimmed = node.dimmed,
                slotSize = slotSize,
                resolvedRoleLabel = stringResource(R.string.kroniq_member),
                member = member,
                imageModel = member?.profileImageUrl,
                onClick = onAddMemberClick,
                onEditClick = onEditMemberClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = centerToStart(maxWidth, node.centerX, slotSize).coerceAtLeast(0.dp),
                        top = (centerToStart(maxHeight, node.centerY, slotSize) - 22.dp).coerceAtLeast(0.dp)
                    )
            )
        }

        middleNodes.forEach { node ->
            val member = resolvedMembers.getOrNull(topNodes.size + middleNodes.indexOf(node))
            MemberColumn(
                label = member?.name?.takeIf { it.isNotBlank() } ?: node.label,
                isResolvedMember = member?.name?.isNotBlank() == true,
                dimmed = node.dimmed,
                slotSize = slotSize,
                resolvedRoleLabel = stringResource(R.string.kroniq_member),
                member = member,
                imageModel = member?.profileImageUrl,
                onClick = onAddMemberClick,
                onEditClick = onEditMemberClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = centerToStart(maxWidth, node.centerX, slotSize).coerceAtLeast(0.dp),
                        top = (centerToStart(maxHeight, node.centerY, slotSize) - 22.dp).coerceAtLeast(0.dp)
                    )
            )
        }

        bottomNodes.forEach { node ->
            val member = resolvedGuests.getOrNull(bottomNodes.indexOf(node))
            MemberColumn(
                label = member?.name?.takeIf { it.isNotBlank() } ?: node.label,
                isResolvedMember = member?.name?.isNotBlank() == true,
                dimmed = node.dimmed,
                slotSize = slotSize,
                resolvedRoleLabel = stringResource(R.string.kroniq_guest),
                member = member,
                imageModel = member?.profileImageUrl,
                onClick = onAddGuestClick,
                onEditClick = onEditMemberClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = centerToStart(maxWidth, node.centerX, slotSize).coerceAtLeast(0.dp),
                        top = (centerToStart(maxHeight, node.centerY, slotSize) - 22.dp).coerceAtLeast(0.dp)
                    )
            )
        }

        CenterBrand(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = centerToStart(maxWidth, brandCenterX, 120.dp).coerceAtLeast(0.dp),
                    top = centerToStart(maxHeight, brandCenterY, 120.dp).coerceAtLeast(0.dp)
                )
        )

        MainMemberCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    top = centerToStart(maxHeight, mainMemberCenterY, mainMemberSize + 44.dp).coerceAtLeast(0.dp)
                ),
            imageSize = mainMemberSize,
            memberName = adminMember?.name?.takeIf { it.isNotBlank() } ?: "Kristin",
            imageModel = adminMember?.profileImageUrl
        )
    }
}

@Composable
private fun MemberColumn(
    label: String,
    isResolvedMember: Boolean,
    dimmed: Boolean = false,
    slotSize: Dp,
    resolvedRoleLabel: String = stringResource(R.string.kroniq_member),
    member: KroniqMemberDto? = null,
    imageModel: Any? = null,
    onClick: () -> Unit,
    onEditClick: (KroniqMemberDto) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isResolvedMember) {
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16f.fs(LocalUiScaler.current),
                modifier = Modifier.width(slotSize + 12.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = resolvedRoleLabel,
                color = Color.White.copy(alpha = 0.78f),
                fontWeight = FontWeight.Normal,
                fontSize = 14f.fs(LocalUiScaler.current),
                modifier = Modifier.width(slotSize + 12.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.78f),
                fontWeight = FontWeight.Normal,
                fontSize = 14f.fs(LocalUiScaler.current),
                modifier = Modifier.width(slotSize + 12.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        if (member != null) {
            Box(
                modifier = Modifier.size(slotSize)
            ) {
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.tripdetail_addnewmember),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 12.dp, y = 12.dp)
                        .size(32.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onEditClick(member) }
                        ),
                ) {
                    Image(
                        painter = painterResource(R.drawable.tripdetail_ic_edit),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else {
            Image(
                painter = painterResource(R.drawable.tripdetail_addnewmember),
                contentDescription = null,
                modifier = Modifier
                    .size(slotSize)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    ),
                alpha = if (dimmed) 0.55f else 1f
            )
        }
    }
}

@Composable
private fun CenterBrand(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo_memotrip),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
    }
}

@Composable
private fun MainMemberCard(
    modifier: Modifier = Modifier,
    imageSize: Dp,
    memberName: String,
    imageModel: Any? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF759F67))
                .innerShadow()
                .padding(horizontal = 10.dp, vertical = 2.dp)
        ) {
            Text(
                text = memberName,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14f.fs(LocalUiScaler.current)
            )
        }
        Text(
            text = stringResource(R.string.kroniq_admin),
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 12f.fs(LocalUiScaler.current),
            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
        )
        if (imageModel != null) {
            AsyncImage(
                model = imageModel,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(imageSize)
                    .clip(RoundedCornerShape(10.dp))
            )
        } else {
            Image(
                painter = painterResource(R.drawable.some_avatar_kristin),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(imageSize)
                    .clip(RoundedCornerShape(10.dp))
            )
        }
    }
}

private fun centerToStart(container: Dp, centerFraction: Float, itemSize: Dp): Dp =
    (container * centerFraction) - (itemSize / 2f)

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 412, heightDp = 892)
@Composable
private fun KroniqScreenPreview() {
    CompositionLocalProvider(LocalUiScaler provides PreviewUiScaler) {
        MemoTripTheme {
            KroniqScreen()
        }
    }
}
