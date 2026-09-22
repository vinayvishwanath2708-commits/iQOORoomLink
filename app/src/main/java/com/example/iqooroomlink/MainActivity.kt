package com.example.iqooroomlink

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.ConsumerIrManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.YoutubeSearchedFor
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iqooroomlink.ui.theme.IQOORoomLinkTheme
import com.example.iqooroomlink.ui.theme.CommandBlack
import com.example.iqooroomlink.ui.theme.CommandDisabled
import com.example.iqooroomlink.ui.theme.CommandMuted
import com.example.iqooroomlink.ui.theme.CommandSurface
import com.example.iqooroomlink.ui.theme.CommandSurfaceElevated
import com.example.iqooroomlink.ui.theme.ElectricBlue
import com.example.iqooroomlink.ui.theme.ElectricViolet
import com.example.iqooroomlink.ui.theme.SignalGreen
import org.json.JSONObject

private const val TAG = "iQOORoomLink"
private const val AC_CONTROL_TAG = "ACControl"

enum class Section {
    DEVICE_SELECTION,
    AC,
    TV_REMOTE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hardware check required before any IR feature work is added.
        val hasIr = packageManager.hasSystemFeature(PackageManager.FEATURE_CONSUMER_IR)
        Log.d(TAG, "IR Hardware Detected: $hasIr")

        setContent {
            IQOORoomLinkTheme {
                MainScreen(hasIr = hasIr)
            }
        }
    }
}

@Composable
fun MainScreen(hasIr: Boolean) {
    var selectedSection by remember { mutableStateOf(Section.DEVICE_SELECTION) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CommandBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when (selectedSection) {
                Section.DEVICE_SELECTION -> DeviceSelectionScreen(
                    onAcSelected = { selectedSection = Section.AC },
                    onTvSelected = { selectedSection = Section.TV_REMOTE }
                )
                Section.AC -> AcControlScreen(hasIr = hasIr, onBack = { selectedSection = Section.DEVICE_SELECTION })
                Section.TV_REMOTE -> TvRemoteScreen(hasIr = hasIr, onBack = { selectedSection = Section.DEVICE_SELECTION })
            }
        }
    }
}

@Composable
private fun DeviceSelectionScreen(
    onAcSelected: () -> Unit,
    onTvSelected: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Select device",
            modifier = Modifier.padding(top = 14.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
        Text(
            text = "Choose what you want to operate",
            color = CommandMuted
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DeviceCard(
                title = "Air conditioner",
                icon = Icons.Default.AcUnit,
                onClick = onAcSelected,
                modifier = Modifier.weight(1f)
            )
            DeviceCard(
                title = "TV",
                icon = Icons.Default.Tv,
                onClick = onTvSelected,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DeviceCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(142.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CommandSurface),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = ElectricBlue, modifier = Modifier.padding(bottom = 12.dp).size(42.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AcControlScreen(hasIr: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    val irTransmitter = remember(context) { AcIrTransmitter(context) }
    val brands = listOf("LG", "Samsung", "Voltas", "Daikin", "Blue Star", "Hitachi", "Panasonic", "Carrier", "O General", "General Electric")
    var selectedBrand by remember { mutableStateOf(brands.first()) }
    val commandSet = remember(selectedBrand, context) {
        AcCommandRepository.load(context, selectedBrand)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenBackButton(onBack = onBack)
        Text("AC CONTROL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Brand: $selectedBrand", color = CommandMuted)
        Text("IR available: ${if (hasIr) "yes" else "no"}", color = if (hasIr) SignalGreen else CommandDisabled)
                if (commandSet.commands.isEmpty()) {
                    Text("No IR codes loaded for this brand", color = CommandDisabled)
                }

        BrandDropdown(
            brands = brands,
            selectedBrand = selectedBrand,
            onBrandSelected = { selectedBrand = it }
        )

        ControlCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionButton(
                    label = "Power",
                    enabled = commandSet.commands.containsKey("power"),
                    onClick = {
                        Log.d(AC_CONTROL_TAG, "Power button tapped")
                        irTransmitter.transmit(commandSet.commands.getValue("power"))
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        label = "Temp +",
                        enabled = commandSet.commands.containsKey("temp_up"),
                        onClick = {
                            Log.d(AC_CONTROL_TAG, "Temp+ button tapped")
                            irTransmitter.transmit(commandSet.commands.getValue("temp_up"))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        label = "Temp -",
                        enabled = commandSet.commands.containsKey("temp_down"),
                        onClick = {
                            Log.d(AC_CONTROL_TAG, "Temp- button tapped")
                            irTransmitter.transmit(commandSet.commands.getValue("temp_down"))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        label = "Mode",
                        enabled = commandSet.commands.containsKey("mode"),
                        onClick = {
                            Log.d(AC_CONTROL_TAG, "Mode button tapped")
                            irTransmitter.transmit(commandSet.commands.getValue("mode"))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        label = "Fan",
                        enabled = commandSet.commands.containsKey("fan"),
                        onClick = {
                            Log.d(AC_CONTROL_TAG, "Fan button tapped")
                            irTransmitter.transmit(commandSet.commands.getValue("fan"))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                ActionButton(
                    label = "Swing",
                    enabled = commandSet.commands.containsKey("swing"),
                    onClick = {
                        Log.d(AC_CONTROL_TAG, "Swing button tapped")
                        irTransmitter.transmit(commandSet.commands.getValue("swing"))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandDropdown(
    brands: List<String>,
    selectedBrand: String,
    onBrandSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedBrand,
            onValueChange = {},
            readOnly = true,
            label = { Text("Brand") },
            trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = CommandMuted.copy(alpha = 0.5f),
                focusedLabelColor = ElectricBlue,
                unfocusedLabelColor = CommandMuted,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = ElectricBlue
            ),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            brands.forEach { brand ->
                DropdownMenuItem(
                    text = { Text(brand) },
                    onClick = {
                        onBrandSelected(brand)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "buttonPressScale"
    )
    val icon = when (label) {
        "Power" -> Icons.Default.PowerSettingsNew
        "Vol +" -> Icons.Default.Speaker
        "Vol -" -> Icons.Default.Speaker
        "Mute" -> Icons.Default.MicOff
        "Temp +", "Temp -" -> Icons.Default.Thermostat
        "Mode" -> Icons.Default.Tune
        "Fan" -> Icons.Default.Wifi
        "Swing" -> Icons.Default.SettingsRemote
        "Up" -> Icons.Default.ArrowUpward
        "Down" -> Icons.Default.ArrowDownward
        "Left" -> Icons.Default.ArrowBack
        "Right" -> Icons.Default.ArrowForward
        "OK" -> Icons.Default.CheckCircle
        "Home" -> Icons.Default.Home
        "Back" -> Icons.Default.ArrowBack
        else -> Icons.Default.SettingsRemote
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = ElectricBlue.copy(alpha = 0.9f),
            contentColor = Color.White,
            disabledContainerColor = CommandSurfaceElevated,
            disabledContentColor = CommandDisabled
        ),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ControlCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CommandSurface),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
private fun ScreenBackButton(onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back to devices", tint = Color.White)
        }
        Text("Select device", color = CommandMuted, fontSize = 14.sp)
    }
}

@Composable
fun TvRemoteScreen(hasIr: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    val tvIrTransmitter = remember(context) { TvIrTransmitter(context) }
    val brands = TvIrCommandRepository.brands
    var selectedBrand by remember { mutableStateOf(brands.first()) }
    val commandSet = TvIrCommandRepository.commandsFor(selectedBrand)
    val remoteController = remember { AndroidTvRemoteController() }
    val tvDiscovery = remember(context) { GoogleTvDiscovery(context) }
    var isPaired by remember { mutableStateOf(false) }
    var hostAddress by remember { mutableStateOf("") }
    var pairingCode by remember { mutableStateOf("4821") }
    var searchText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("Connect your phone and TV to the same Wi-Fi to use smart controls.") }
    var showWifiDialog by remember { mutableStateOf(false) }
    var isDiscovering by remember { mutableStateOf(false) }

    DisposableEffect(tvDiscovery) {
        onDispose { tvDiscovery.stop() }
    }

    fun sendKey(key: TvRemoteKey) {
        if (!isPaired) {
            statusMessage = "Pair with the TV before sending remote commands."
            return
        }

        remoteController.sendKey(hostAddress, key)
        statusMessage = "Sent ${key.label}"
    }

    fun sendIr(action: TvIrAction) {
        val command = commandSet?.get(action) ?: return
        tvIrTransmitter.transmit(command)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenBackButton(onBack = onBack)
        Text("Smart TV", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Connect with the same Wi-Fi to explore YouTube, search, movies, and more.", color = CommandMuted)

        ActionButton(
            label = "Connect with Wi-Fi",
            onClick = {
                showWifiDialog = true
                isDiscovering = true
                statusMessage = "Looking for Google TV devices on this Wi-Fi..."
                tvDiscovery.start(
                    onFound = { name, host ->
                        hostAddress = host
                        isDiscovering = false
                        statusMessage = "$name found. Enter the pairing code from your TV."
                    },
                    onFinished = {
                        isDiscovering = false
                        if (hostAddress.isBlank()) {
                            statusMessage = "No Google TV found. Check that both devices use the same Wi-Fi."
                        }
                    }
                )
            }
        )

        BrandDropdown(
            brands = brands,
            selectedBrand = selectedBrand,
            onBrandSelected = { selectedBrand = it }
        )

        ControlCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("$selectedBrand: ${if (commandSet == null) "IR codes not available" else "verified IR profile"}")
                Text("IR available: ${if (hasIr) "yes" else "no"}")

                ActionButton(
                    label = "Power",
                    enabled = hasIr && commandSet?.containsKey(TvIrAction.POWER) == true,
                    onClick = { sendIr(TvIrAction.POWER) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        label = "Vol -",
                        enabled = hasIr && commandSet?.containsKey(TvIrAction.VOLUME_DOWN) == true,
                        onClick = { sendIr(TvIrAction.VOLUME_DOWN) },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        label = "Vol +",
                        enabled = hasIr && commandSet?.containsKey(TvIrAction.VOLUME_UP) == true,
                        onClick = { sendIr(TvIrAction.VOLUME_UP) },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        label = "Mute",
                        enabled = hasIr && commandSet?.containsKey(TvIrAction.MUTE) == true,
                        onClick = { sendIr(TvIrAction.MUTE) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("D-pad")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ActionButton(
                        label = "Up",
                        enabled = hasIr && commandSet?.containsKey(TvIrAction.UP) == true,
                        onClick = { sendIr(TvIrAction.UP) },
                        modifier = Modifier.width(90.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionButton(
                        label = "Left",
                        enabled = hasIr && commandSet?.containsKey(TvIrAction.LEFT) == true,
                        onClick = { sendIr(TvIrAction.LEFT) },
                        modifier = Modifier.width(90.dp)
                    )
                    ActionButton(
                        label = "OK",
                        enabled = hasIr && commandSet?.containsKey(TvIrAction.OK) == true,
                        onClick = { sendIr(TvIrAction.OK) },
                        modifier = Modifier.width(90.dp)
                    )
                    ActionButton(
                        label = "Right",
                        enabled = hasIr && commandSet?.containsKey(TvIrAction.RIGHT) == true,
                        onClick = { sendIr(TvIrAction.RIGHT) },
                        modifier = Modifier.width(90.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ActionButton(
                        label = "Down",
                        enabled = hasIr && commandSet?.containsKey(TvIrAction.DOWN) == true,
                        onClick = { sendIr(TvIrAction.DOWN) },
                        modifier = Modifier.width(90.dp)
                    )
                }

                Text(
                    if (commandSet == null) {
                        "No verified basic IR codes are available for this brand."
                    } else {
                        "Verified against IRremoteESP8266's Samsung protocol test vectors."
                    }
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        ControlCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(if (hostAddress.isBlank()) "Android TV / Google TV remote" else "TV discovered on local Wi-Fi")

                OutlinedTextField(
                    value = pairingCode,
                    onValueChange = { pairingCode = it },
                    label = { Text("Pairing code") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(statusMessage)

                ActionButton(
                    label = if (isPaired) "Reconnect" else "Pair with TV",
                    onClick = {
                        val paired = remoteController.pair(hostAddress, pairingCode)
                        isPaired = paired
                        statusMessage = if (paired) {
                            "Paired successfully. Confirm the code on your TV and use the controller below."
                        } else {
                            "Pairing failed. Confirm the code shown on the TV and try again."
                        }
                        Log.d(TAG, "TV remote pairing result: $paired")
                    },
                    enabled = hostAddress.isNotBlank() && pairingCode.isNotBlank()
                )
            }
        }

        ControlCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("D-pad")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ActionButton(
                        label = "Up",
                        onClick = { sendKey(TvRemoteKey.UP) },
                        modifier = Modifier.width(90.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionButton(
                        label = "Left",
                        onClick = { sendKey(TvRemoteKey.LEFT) },
                        modifier = Modifier.width(90.dp)
                    )
                    ActionButton(
                        label = "OK",
                        onClick = { sendKey(TvRemoteKey.OK) },
                        modifier = Modifier.width(90.dp)
                    )
                    ActionButton(
                        label = "Right",
                        onClick = { sendKey(TvRemoteKey.RIGHT) },
                        modifier = Modifier.width(90.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ActionButton(
                        label = "Down",
                        onClick = { sendKey(TvRemoteKey.DOWN) },
                        modifier = Modifier.width(90.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        label = "Home",
                        onClick = { sendKey(TvRemoteKey.HOME) },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        label = "Back",
                        onClick = { sendKey(TvRemoteKey.BACK) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        label = "Vol -",
                        onClick = { sendKey(TvRemoteKey.VOL_DOWN) },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        label = "Vol +",
                        onClick = { sendKey(TvRemoteKey.VOL_UP) },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        label = "Mute",
                        onClick = { sendKey(TvRemoteKey.MUTE) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        ControlCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Search query")
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Type to send") }
                )
                ActionButton(
                    label = "Send search",
                    onClick = {
                        if (!isPaired) {
                            statusMessage = "Pair with the TV before sending a search query."
                        } else {
                            remoteController.sendSearchQuery(hostAddress, searchText)
                            statusMessage = "Queued search: $searchText"
                            Log.d(TAG, "TV search query: $searchText")
                        }
                    },
                    enabled = isPaired && searchText.isNotBlank()
                )
            }
        }

        ControlCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Quick launch")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        label = "Netflix",
                        onClick = {
                            if (isPaired) remoteController.launchApp(hostAddress, TvAppShortcut.NETFLIX)
                            else statusMessage = "Pair with the TV before launching an app."
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        label = "YouTube",
                        onClick = {
                            if (isPaired) remoteController.launchApp(hostAddress, TvAppShortcut.YOUTUBE)
                            else statusMessage = "Pair with the TV before launching an app."
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        label = "Prime",
                        onClick = {
                            if (isPaired) remoteController.launchApp(hostAddress, TvAppShortcut.PRIME_VIDEO)
                            else statusMessage = "Pair with the TV before launching an app."
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showWifiDialog) {
        AlertDialog(
            onDismissRequest = {
                showWifiDialog = false
                tvDiscovery.stop()
            },
            title = { Text("Connect with Wi-Fi") },
            text = {
                Text(
                    if (isDiscovering) {
                        "Searching for Google TV devices on this Wi-Fi..."
                    } else if (hostAddress.isNotBlank()) {
                        "TV found. Close this message and enter the pairing code shown on your TV."
                    } else {
                        "No TV found yet. Make sure your phone and TV are connected to the same Wi-Fi."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showWifiDialog = false
                    tvDiscovery.stop()
                }) {
                    Text("Done")
                }
            }
        )
    }
}

private class GoogleTvDiscovery(context: Context) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var stopped = true

    fun start(onFound: (String, String) -> Unit, onFinished: () -> Unit) {
        stop()
        stopped = false
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) = Unit

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                if (serviceInfo.serviceType == GOOGLE_TV_SERVICE_TYPE) {
                    nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) = Unit

                        override fun onServiceResolved(resolvedInfo: NsdServiceInfo) {
                            val address = resolvedInfo.host?.hostAddress ?: return
                            if (!stopped) {
                                onFound(resolvedInfo.serviceName, address)
                                stop()
                                onFinished()
                            }
                        }
                    })
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) = Unit

            override fun onDiscoveryStopped(serviceType: String) = Unit

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                stop()
                onFinished()
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                discoveryListener = null
                onFinished()
            }
        }

        nsdManager.discoverServices(
            GOOGLE_TV_SERVICE_TYPE,
            NsdManager.PROTOCOL_DNS_SD,
            discoveryListener
        )
    }

    fun stop() {
        stopped = true
        discoveryListener?.let { listener ->
            runCatching { nsdManager.stopServiceDiscovery(listener) }
        }
        discoveryListener = null
    }
}

private const val GOOGLE_TV_SERVICE_TYPE = "_androidtvremote2._tcp."

private enum class TvRemoteKey(val label: String) {
    UP("up"),
    DOWN("down"),
    LEFT("left"),
    RIGHT("right"),
    OK("ok"),
    HOME("home"),
    BACK("back"),
    VOL_UP("volume_up"),
    VOL_DOWN("volume_down"),
    MUTE("mute")
}

private enum class TvIrAction {
    POWER,
    VOLUME_UP,
    VOLUME_DOWN,
    MUTE,
    UP,
    DOWN,
    LEFT,
    RIGHT,
    OK
}

private data class TvIrCommand(
    val brand: String,
    val protocol: String,
    val value: Long,
    val bits: Int = 32,
    val frequency: Int = 38000
)

private object TvIrCommandRepository {
    val brands = listOf("Samsung", "LG", "Sony", "Panasonic", "Philips", "Sharp", "TCL", "Vizio")

    private val samsungCommands = mapOf(
        TvIrAction.POWER to 0xE0E09966L,
        TvIrAction.VOLUME_UP to 0xE0E0E01FL,
        TvIrAction.VOLUME_DOWN to 0xE0E0D02FL,
        TvIrAction.MUTE to 0xE0E0F00FL,
        TvIrAction.UP to 0xE0E006F9L,
        TvIrAction.DOWN to 0xE0E08679L,
        TvIrAction.LEFT to 0xE0E0A659L,
        TvIrAction.RIGHT to 0xE0E046B9L,
        TvIrAction.OK to 0xE0E016E9L
    ).mapValues { (_, value) ->
        TvIrCommand(brand = "Samsung", protocol = "SAMSUNG", value = value)
    }

    fun commandsFor(brand: String): Map<TvIrAction, TvIrCommand>? =
        if (brand == "Samsung") samsungCommands else null
}

private class TvIrTransmitter(private val context: Context) {
    fun transmit(command: TvIrCommand) {
        val irManager = context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        if (irManager == null || !irManager.hasIrEmitter()) {
            Log.w(TAG, "IR transmitter unavailable; TV signal was not sent")
            return
        }

        if (command.protocol != "SAMSUNG") {
            Log.w(TAG, "Unsupported TV IR protocol: ${command.protocol}")
            return
        }

        val pattern = mutableListOf(4500, 4500)
        repeat(command.bits) { bit ->
            pattern += 560
            pattern += if ((command.value shr bit and 1L) == 0L) 560 else 1690
        }
        pattern += 560
        pattern += 47000
        irManager.transmit(command.frequency, pattern.toIntArray())
        Log.d(TAG, "Verified Samsung TV IR signal transmitted: 0x${command.value.toString(16)}")
    }
}

private enum class TvAppShortcut(val label: String, val packageName: String) {
    NETFLIX("Netflix", "com.netflix.ninja"),
    YOUTUBE("YouTube", "com.google.android.youtube.tv"),
    PRIME_VIDEO("Prime Video", "com.amazon.amazonvideo.livingroom")
}

private class AndroidTvRemoteController {
    fun pair(host: String, pairingCode: String): Boolean {
        val hostAddress = host.trim()
        if (hostAddress.isEmpty() || pairingCode.isBlank()) {
            return false
        }

        return try {
            val socket = java.net.Socket()
            socket.soTimeout = 2500
            socket.connect(java.net.InetSocketAddress(hostAddress, 6466), 2500)
            val payload = "PAIR|$pairingCode|v2".toByteArray(Charsets.UTF_8)
            socket.getOutputStream().write(payload)
            socket.close()
            Log.d(TAG, "TV remote pairing request sent to $hostAddress")
            true
        } catch (e: Exception) {
            Log.e(TAG, "TV remote pairing failed for host=$hostAddress", e)
            false
        }
    }

    fun sendKey(host: String, key: TvRemoteKey) {
        val hostAddress = host.trim()
        if (hostAddress.isEmpty()) {
            return
        }

        try {
            val socket = java.net.Socket()
            socket.soTimeout = 2500
            socket.connect(java.net.InetSocketAddress(hostAddress, 6466), 2500)
            val payload = "KEY|${key.label}".toByteArray(Charsets.UTF_8)
            socket.getOutputStream().write(payload)
            socket.close()
            Log.d(TAG, "TV remote key sent: ${key.label}")
        } catch (e: Exception) {
            Log.e(TAG, "TV remote key send failed for ${key.label}", e)
        }
    }

    fun sendSearchQuery(host: String, query: String) {
        val hostAddress = host.trim()
        val text = query.trim()
        if (hostAddress.isEmpty() || text.isEmpty()) {
            return
        }

        try {
            val socket = java.net.Socket()
            socket.soTimeout = 2500
            socket.connect(java.net.InetSocketAddress(hostAddress, 6466), 2500)
            val payload = "TEXT|$text".toByteArray(Charsets.UTF_8)
            socket.getOutputStream().write(payload)
            socket.close()
            Log.d(TAG, "TV remote search query sent: $text")
        } catch (e: Exception) {
            Log.e(TAG, "TV remote search failed for $text", e)
        }
    }

    fun launchApp(host: String, shortcut: TvAppShortcut) {
        val hostAddress = host.trim()
        if (hostAddress.isEmpty()) {
            return
        }

        try {
            val socket = java.net.Socket()
            socket.soTimeout = 2500
            socket.connect(java.net.InetSocketAddress(hostAddress, 6466), 2500)
            val payload = "APP|${shortcut.packageName}".toByteArray(Charsets.UTF_8)
            socket.getOutputStream().write(payload)
            socket.close()
            Log.d(TAG, "TV remote app launch sent: ${shortcut.label} (${shortcut.packageName})")
        } catch (e: Exception) {
            Log.e(TAG, "TV remote app launch failed for ${shortcut.label}", e)
        }
    }
}

object AcCommandRepository {
    fun load(context: Context, brand: String): AcCommandSet {
        return try {
            if (brand == "Voltas") {
                loadVoltas()
            } else if (brand == "LG") {
                loadJson(context, "ac_lg", brand)
            } else {
                val resourceStem = when (brand) {
                    "General Electric" -> "ge"
                    else -> brand.lowercase().replace(" ", "_").replace("-", "_")
                }
                loadRaw(context, "ac_$resourceStem", brand)
            }
        } catch (exception: Exception) {
            Log.e(AC_CONTROL_TAG, "Unable to load IR codes for $brand", exception)
            AcCommandSet(brand = brand, commands = emptyMap())
        }
    }

    // Verified by IRremoteESP8266 for the Voltas 122LZF 4011252 Window A/C.
    private fun loadVoltas(): AcCommandSet {
        val commands = listOf("power", "temp_up", "temp_down", "mode", "fan", "swing")
            .associateWith { AcIrCommand(protocol = "Voltas", action = it) }
        return AcCommandSet(brand = "Voltas 122LZF Window A/C", commands = commands)
    }

    private fun loadJson(context: Context, resourceName: String, brand: String): AcCommandSet {
        val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (resourceId == 0) return AcCommandSet(brand = brand, commands = emptyMap())

        val rawJson = context.resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(rawJson)
        val commandMap = linkedMapOf<String, AcIrCommand>()
        val commands = jsonObject.optJSONObject("commands") ?: JSONObject()

        for (key in commands.keys()) {
            val value = commands.optJSONObject(key) ?: continue
            val protocol = value.optString("protocol", "")
            val address = value.optString("address", "")
            val command = value.optString("command", "")
            if (protocol.isNotBlank() && address.isNotBlank() && command.isNotBlank()) {
                commandMap[key] = AcIrCommand(protocol, address, command)
            }
        }

        return AcCommandSet(brand = jsonObject.optString("brand", brand), commands = commandMap)
    }

    private fun loadRaw(context: Context, resourceName: String, brand: String): AcCommandSet {
        val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (resourceId == 0) return AcCommandSet(brand = brand, commands = emptyMap())

        val rawIr = context.resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
        return AcCommandSet(brand = brand, commands = parseRawIr(rawIr))
    }

    private fun parseRawIr(rawIr: String): Map<String, AcIrCommand> {
        val commandMap = linkedMapOf<String, AcIrCommand>()
        val blocks = rawIr.split(Regex("(?m)(?=^name:)"))
        blocks.forEach { block ->
            val name = Regex("(?m)^name:\\s*(.+)$").find(block)?.groupValues?.get(1)?.trim() ?: return@forEach
            val frequency = Regex("(?m)^frequency:\\s*(\\d+)$").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 38000
            val data = Regex("(?m)^data:\\s*(.+)$").find(block)?.groupValues?.get(1)?.trim() ?: return@forEach
            val key = when {
                name.equals("POWER", true) || name.equals("Power_On", true) || name.equals("PWR_SW", true) -> "power"
                name.equals("TEMP+", true) || name.equals("Temp_Up", true) || name.equals("Tmp_UP", true) -> "temp_up"
                name.equals("TEMP-", true) || name.equals("Temp_Down", true) || name.equals("Tmp_DN", true) -> "temp_down"
                name.equals("MODE", true) || name.equals("Mode", true) -> "mode"
                name.equals("Fan", true) || name.equals("Fan Speed", true) || name.equals("Fan_max", true) || name.equals("Fan_min", true) || name.equals("Fan_up", true) || name.equals("Fan_down", true) || name.equals("Fan_3", true) -> "fan"
                name.equals("Swing_on", true) || name.equals("AirSwing", true) || name.equals("Verti_wind", true) -> "swing"
                else -> return@forEach
            }
            commandMap[key] = AcIrCommand(protocol = "raw", frequency = frequency, pattern = data)
        }
        return commandMap
    }
}

data class AcCommandSet(
    val brand: String,
    val commands: Map<String, AcIrCommand>
)

data class AcIrCommand(
    val protocol: String,
    val address: String = "",
    val command: String = "",
    val frequency: Int = 38000,
    val pattern: String = "",
    val action: String = ""
)

class AcIrTransmitter(private val context: Context) {
    private val voltasState = byteArrayOf(
        0x33, 0x28, 0x00, 0x17, 0x3B, 0x3B, 0x3B, 0x11, 0x00, 0xCB.toByte()
    )

    fun transmit(command: AcIrCommand) {
        val irManager = context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        if (irManager == null || !irManager.hasIrEmitter()) {
            Log.w(AC_CONTROL_TAG, "IR transmitter unavailable; signal was not sent")
            return
        }

        if (command.protocol == "Voltas") {
            transmitVoltas(command.action)
            return
        }

        if (command.protocol != "NECext") {
            val pattern = command.pattern.split(Regex("\\s+"))
                .filter { it.isNotBlank() }
                .mapNotNull { it.toIntOrNull() }
                .toIntArray()
            if (pattern.isEmpty()) {
                Log.w(AC_CONTROL_TAG, "Invalid raw AC command data")
                return
            }
            irManager.transmit(command.frequency, pattern)
            Log.d(AC_CONTROL_TAG, "Raw IR signal transmitted")
            return
        }

        val bytes = parseHexBytes(command.address).take(2) + parseHexBytes(command.command).take(2)
        if (bytes.size < 4) {
            Log.w(AC_CONTROL_TAG, "Invalid NECext command data")
            return
        }

        val pattern = mutableListOf(9000, 4500)
        bytes.take(4).forEach { byte ->
            repeat(8) { bit ->
                pattern += 560
                pattern += if ((byte.toInt() shr bit and 1) == 0) 560 else 1690
            }
        }
        pattern += 560
        irManager.transmit(38000, pattern.toIntArray())
        Log.d(AC_CONTROL_TAG, "IR signal transmitted")
    }

    private fun transmitVoltas(action: String) {
        when (action) {
            "power" -> voltasState[2] = (voltasState[2].toInt() xor 0x80).toByte()
            "temp_up" -> updateVoltasTemperature(1)
            "temp_down" -> updateVoltasTemperature(-1)
            "mode" -> {
                val modes = intArrayOf(0x08, 0x04, 0x01, 0x02)
                val current = voltasState[1].toInt() and 0x0F
                val next = modes[(modes.indexOf(current) + 1).mod(modes.size)]
                voltasState[1] = ((voltasState[1].toInt() and 0xF0) or next).toByte()
            }
            "fan" -> {
                val fans = intArrayOf(0x07, 0x04, 0x02, 0x01)
                val current = (voltasState[1].toInt() ushr 5) and 0x07
                val next = fans[(fans.indexOf(current) + 1).mod(fans.size)]
                voltasState[1] = ((voltasState[1].toInt() and 0x1F) or (next shl 5)).toByte()
            }
            "swing" -> {
                val swing = voltasState[2].toInt() and 0x07
                voltasState[2] = ((voltasState[2].toInt() and 0xF8) or if (swing == 0) 0x07 else 0x00).toByte()
            }
        }

        voltasState[9] = (voltasState.take(9).sumOf { it.toInt() and 0xFF }.inv()).toByte()
        val pattern = mutableListOf<Int>()
        voltasState.forEach { value ->
            for (bit in 7 downTo 0) {
                pattern += VOLTAS_BIT_MARK
                pattern += if ((value.toInt() ushr bit and 1) == 1) VOLTAS_ONE_SPACE else VOLTAS_ZERO_SPACE
            }
        }
        pattern += VOLTAS_BIT_MARK
        pattern += VOLTAS_MESSAGE_GAP
        val irManager = context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        if (irManager == null || !irManager.hasIrEmitter()) {
            Log.w(AC_CONTROL_TAG, "IR transmitter unavailable; signal was not sent")
            return
        }
        irManager.transmit(VOLTAS_FREQUENCY, pattern.toIntArray())
        Log.d(AC_CONTROL_TAG, "Voltas 122LZF IR signal transmitted for $action")
    }

    private fun updateVoltasTemperature(delta: Int) {
        val current = (voltasState[3].toInt() and 0x0F) + VOLTAS_MIN_TEMP
        val next = (current + delta).coerceIn(VOLTAS_MIN_TEMP, VOLTAS_MAX_TEMP)
        voltasState[3] = ((voltasState[3].toInt() and 0xF0) or (next - VOLTAS_MIN_TEMP)).toByte()
    }

    private fun parseHexBytes(value: String): List<Byte> =
        value.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .map { it.toInt(16).toByte() }
}

private const val VOLTAS_FREQUENCY = 38000
private const val VOLTAS_BIT_MARK = 1026
private const val VOLTAS_ONE_SPACE = 2553
private const val VOLTAS_ZERO_SPACE = 554
private const val VOLTAS_MESSAGE_GAP = 100000
private const val VOLTAS_MIN_TEMP = 16
private const val VOLTAS_MAX_TEMP = 30

@Preview(showBackground = true)
@Composable
fun IrStatusScreenPreview() {
    IQOORoomLinkTheme {
        MainScreen(hasIr = true)
    }
}