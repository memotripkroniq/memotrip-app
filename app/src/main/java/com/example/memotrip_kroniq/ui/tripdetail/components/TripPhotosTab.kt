package com.example.memotrip_kroniq.ui.tripdetail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.home.components.modifiers.innerTopShadow

@Composable
fun TripPhotosTab(
    modifier: Modifier = Modifier,
    categories: List<TripPhotoCategoryUi>,
    photos: List<TripPhotoUi>,
    isLoading: Boolean,
    onAddCategoryClick: (String) -> Unit,
    onRenameCategoryClick: (String, String) -> Unit,
    onDeleteCategoryClick: (String) -> Unit,
    onAddPhotoClick: (String?) -> Unit,
    onDeletePhotoClick: (String) -> Unit,
) {
    val s = LocalUiScaler.current
    val visibleCategories = remember(categories) {
        categories.filterNot { it.name.equals("Uncategorized", ignoreCase = true) }
    }

    var selectedCategoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPhoto by remember { mutableStateOf<TripPhotoUi?>(null) }
    var categoryDialogState by remember { mutableStateOf<PhotoCategoryDialogState?>(null) }

    LaunchedEffect(visibleCategories) {
        if (selectedCategoryId == null) {
            selectedCategoryId = visibleCategories.firstOrNull()?.id
        } else if (visibleCategories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = visibleCategories.firstOrNull()?.id
        }
    }

    val filteredPhotos = remember(photos, selectedCategoryId) {
        val selected = selectedCategoryId
        if (selected.isNullOrBlank()) photos else photos.filter { it.categoryId == selected }
    }

    Box(
        modifier = modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 84.dp)
        ) {
            Text(
                text = stringResource(R.string.trip_detail_photos_categories_title),
                color = Color.White,
                fontSize = 16f.fs(s),
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12f.sy(s)))

            PhotoCategoryRow(
                categories = visibleCategories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { selectedCategoryId = it },
                onAddCategoryClick = {
                    categoryDialogState = PhotoCategoryDialogState.Create("")
                },
                onEditCategoryClick = { category ->
                    categoryDialogState = PhotoCategoryDialogState.Edit(
                        categoryId = category.id,
                        value = category.name
                    )
                }
            )

            Spacer(Modifier.height(18f.sy(s)))

            if (filteredPhotos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF383A41), RoundedCornerShape(14.dp))
                        .innerTopShadow(alpha = 0.18f, height = 18f)
                        .padding(horizontal = 20.dp, vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.trip_detail_photos_empty),
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 16f.fs(s),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                PhotoGrid(
                    photos = filteredPhotos,
                    onPhotoClick = { photoId ->
                        selectedPhoto = filteredPhotos.firstOrNull { it.id == photoId }
                    }
                )
            }
        }

        AddGalleryPhotoButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            onClick = { onAddPhotoClick(selectedCategoryId) }
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }

    selectedPhoto?.let { photo ->
        ZoomableImageDialog(
            imageUrl = photo.imageUrl,
            onDismiss = { selectedPhoto = null },
            onDeleteClick = {
                onDeletePhotoClick(photo.id)
                selectedPhoto = null
            }
        )
    }

    categoryDialogState?.let { dialogState ->
        PhotoCategoryDialog(
            state = dialogState,
            onDismiss = { categoryDialogState = null },
            onConfirm = { value ->
                val trimmed = value.trim()
                if (trimmed.isBlank()) return@PhotoCategoryDialog
                when (dialogState) {
                    is PhotoCategoryDialogState.Create -> onAddCategoryClick(trimmed)
                    is PhotoCategoryDialogState.Edit ->
                        onRenameCategoryClick(dialogState.categoryId, trimmed)
                }
                categoryDialogState = null
            },
            onDelete = {
                val editing = dialogState as? PhotoCategoryDialogState.Edit ?: return@PhotoCategoryDialog
                onDeleteCategoryClick(editing.categoryId)
                categoryDialogState = null
            }
        )
    }
}

@Composable
private fun PhotoCategoryRow(
    categories: List<TripPhotoCategoryUi>,
    selectedCategoryId: String?,
    onCategorySelected: (String) -> Unit,
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (TripPhotoCategoryUi) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AddCategoryCard(
            modifier = Modifier
                .width(92.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onAddCategoryClick() }
        )

        categories.forEach { category ->
            PhotoCategoryCard(
                title = category.name,
                selected = category.id == selectedCategoryId,
                modifier = Modifier.width(92.dp),
                onClick = { onCategorySelected(category.id) },
                onEditClick = { onEditCategoryClick(category) }
            )
        }
    }
}

@Composable
private fun PhotoCategoryCard(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    val s = LocalUiScaler.current
    val borderColor = if (selected) Color(0xFFFFFFFF) else Color(0xFF747781)

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .width(92.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black, RoundedCornerShape(10.dp))
                .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14f.fs(s),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Image(
            painter = painterResource(R.drawable.tripdetail_ic_edit),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 2.dp)
                .size(22.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onEditClick() }
        )
    }
}

@Composable
private fun AddCategoryCard(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.height(92.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.add_trip_name_field),
            contentDescription = stringResource(R.string.trip_detail_photos_add_category),
            modifier = Modifier.size(80.dp)
        )
    }
}

@Composable
private fun PhotoGrid(
    photos: List<TripPhotoUi>,
    onPhotoClick: (String) -> Unit,
) {
    val heroPhoto = photos.firstOrNull()
    val topRightPhotos = photos.drop(1).take(4)
    val remainingRows = photos.drop(5).chunked(4)

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        heroPhoto?.let { hero ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PhotoTile(
                    photo = hero,
                    modifier = Modifier
                        .weight(2f)
                        .aspectRatio(1f),
                    onPhotoClick = onPhotoClick
                )

                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(2) { rowIndex ->
                        val rowItems = topRightPhotos.drop(rowIndex * 2).take(2)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { photo ->
                                PhotoTile(
                                    photo = photo,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f),
                                    onPhotoClick = onPhotoClick
                                )
                            }

                            repeat(2 - rowItems.size) {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        remainingRows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { photo ->
                    PhotoTile(
                        photo = photo,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        onPhotoClick = onPhotoClick
                    )
                }

                repeat(4 - rowItems.size) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoTile(
    photo: TripPhotoUi,
    modifier: Modifier = Modifier,
    onPhotoClick: (String) -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF383A41), RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onPhotoClick(photo.id) }
    ) {
        AsyncImage(
            model = photo.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun AddGalleryPhotoButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.add_trip_name_field),
            contentDescription = stringResource(R.string.trip_detail_photos_add_photo),
            modifier = Modifier
                .size(80.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() }
        )
    }
}

private sealed interface PhotoCategoryDialogState {
    data class Create(val value: String) : PhotoCategoryDialogState
    data class Edit(val categoryId: String, val value: String) : PhotoCategoryDialogState
}

@Composable
private fun PhotoCategoryDialog(
    state: PhotoCategoryDialogState,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var value by remember(state) {
        mutableStateOf(
            when (state) {
                is PhotoCategoryDialogState.Create -> state.value
                is PhotoCategoryDialogState.Edit -> state.value
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    if (state is PhotoCategoryDialogState.Create)
                        R.string.trip_detail_photos_create_category_title
                    else
                        R.string.trip_detail_photos_edit_category_title
                )
            )
        },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                label = { Text(stringResource(R.string.trip_detail_photos_category_name_label)) }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) {
                Text(stringResource(R.string.trip_detail_photos_dialog_save))
            }
        },
        dismissButton = {
            Row {
                if (state is PhotoCategoryDialogState.Edit) {
                    TextButton(onClick = onDelete) {
                        Text(stringResource(R.string.trip_detail_photos_dialog_delete))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.trip_detail_photos_dialog_cancel))
                }
            }
        }
    )
}
