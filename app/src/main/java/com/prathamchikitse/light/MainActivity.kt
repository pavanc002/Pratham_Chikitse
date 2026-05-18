package com.prathamchikitse.light

import android.app.Activity
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private val primary = Color.rgb(192, 57, 43)
    private val primaryDark = Color.rgb(158, 32, 22)
    private val secondary = Color.rgb(0, 109, 55)
    private val warning = Color.rgb(243, 156, 18)
    private val surface = Color.rgb(247, 249, 255)
    private val card = Color.WHITE
    private val surfaceContainer = Color.rgb(227, 239, 254)
    private val textColor = Color.rgb(17, 29, 39)
    private val muted = Color.rgb(89, 65, 61)
    private val error = Color.rgb(186, 26, 26)

    private var tts: TextToSpeech? = null
    private var language: AppLanguage = AppLanguage.KANNADA
    private var currentScreen: Screen = Screen.Language
    private val mainHandler = Handler(Looper.getMainLooper())
    private val profilePrefs: SharedPreferences by lazy { getSharedPreferences("profile", Context.MODE_PRIVATE) }
    private var onlineHospitals: List<Hospital> = emptyList()
    private var hospitalStatus: String = ""
    private var hospitalFetchStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        showLanguage()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("kn", "IN")
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        when (currentScreen) {
            Screen.Language -> super.onBackPressed()
            Screen.Login -> showLanguage()
            Screen.Home -> showLogin()
            is Screen.Guide -> showHome()
            Screen.Hospitals -> showHome()
            Screen.Profile -> showHome()
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            hospitalFetchStarted = false
            if (hasLocationPermission()) {
                showNearbyHospitals()
            } else {
                Toast.makeText(this, "Location permission is needed for nearby hospitals.", Toast.LENGTH_LONG).show()
                showNearbyHospitals()
            }
        }
    }

    private fun showLanguage() {
        currentScreen = Screen.Language
        setContent("ಭಾಷೆ ಆಯ್ಕೆ", showBottomNav = false) { body ->
            body.gravity = Gravity.CENTER_HORIZONTAL
            body.addView(title("ನಿಮ್ಮ ಭಾಷೆಯನ್ನು ಆರಿಸಿ", "Choose your language"))
            body.addView(hero("ಪ್ರಥಮ ಚಿಕಿತ್ಸೆ", "Offline emergency guide"))
            body.addView(bigButton("ಕನ್ನಡ", "KANNADA", primary) {
                language = AppLanguage.KANNADA
                showLogin()
            })
            body.addView(bigButton("ENGLISH", "ಇಂಗ್ಲಿಷ್", primaryDark, outlined = true) {
                language = AppLanguage.ENGLISH
                showLogin()
            })
        }
    }

    private fun showLogin() {
        currentScreen = Screen.Login
        setContent(label("Welcome", "ಸ್ವಾಗತ"), showBottomNav = false) { body ->
            body.addView(title(label("Welcome", "ಸ್ವಾಗತ"), label("Continue for offline first-aid support", "ತುರ್ತು ಸಮಯದಲ್ಲಿ ಸಹಾಯಕ್ಕಾಗಿ ಮುಂದುವರಿಯಿರಿ")))
            body.addView(labelView(label("Phone number", "ಫೋನ್ ಸಂಖ್ಯೆ"), primaryDark, 14f, true))
            body.addView(EditText(this).apply {
                hint = "9876543210"
                inputType = android.text.InputType.TYPE_CLASS_PHONE
                textSize = 20f
                setTextColor(textColor)
                background = roundedStroke(card, Color.rgb(141, 112, 108), 2, 12)
                setPadding(dp(16), dp(8), dp(16), dp(8))
                minHeight = dp(56)
            }, matchWrap())
            body.addView(infoCard(
                label("Location permission", "ಸ್ಥಳ ಅನುಮತಿ"),
                label("Keep this on to quickly view nearby hospitals. The app still works offline.", "ಹತ್ತಿರದ ಆಸ್ಪತ್ರೆಗಳನ್ನು ಬೇಗ ನೋಡಲು ಇದನ್ನು ಸಕ್ರಿಯವಾಗಿಡಿ. ಆಫ್‌ಲೈನ್‌ನಲ್ಲೂ ಅಪ್ಲಿಕೇಶನ್ ಕೆಲಸ ಮಾಡುತ್ತದೆ."),
                secondary,
                trailing = Switch(this).apply {
                    isChecked = hasLocationPermission()
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked && !hasLocationPermission()) requestLocationPermission()
                    }
                }
            ))
            body.addView(bigButton(label("Login", "ಲಾಗಿನ್"), "", primary) { showHome() })
            body.addView(bigButton(label("Continue as guest", "ಅತಿಥಿಯಾಗಿ ಮುಂದುವರಿಯಿರಿ"), "", primaryDark, outlined = true) { showHome() })
        }
    }

    private fun showHome() {
        currentScreen = Screen.Home
        setContent(label("Home", "ಮನೆ"), showBottomNav = true) { body ->
            body.addView(title(label("What happened?", "ಏನು ಸಂಭವಿಸಿದೆ?"), label("Tap the emergency type for quick steps", "ತ್ವರಿತ ಹಂತಗಳಿಗಾಗಿ ಸಮಸ್ಯೆಯನ್ನು ಆಯ್ಕೆಮಾಡಿ")))
            val grid = GridLayout(this).apply {
                columnCount = 2
                useDefaultMargins = true
            }
            guides.forEach { guide -> grid.addView(tile(guide) { showGuide(guide) }) }
            body.addView(grid, matchWrap())
            body.addView(infoCard(
                label("Offline ready", "ಆಫ್‌ಲೈನ್ ಸಿದ್ಧ"),
                label("All DOs and DON'Ts are stored inside the app for use without internet.", "ಇಂಟರ್ನೆಟ್ ಇಲ್ಲದಿದ್ದರೂ ಬಳಸಲು ಎಲ್ಲಾ ಮಾಡಬೇಕಾದವು ಮತ್ತು ಮಾಡಬಾರದವುಗಳು ಅಪ್ಲಿಕೇಶನ್ ಒಳಗೆ ಉಳಿಸಲಾಗಿದೆ."),
                secondary
            ))
        }
    }

    private fun showGuide(guide: Guide) {
        currentScreen = Screen.Guide(guide.id)
        setContent(guide.title(language), showBottomNav = true) { body ->
            body.addView(guideHeader(guide))
            body.addView(checklistSection(label("DOs", "ಮಾಡಿ"), guide.dos, secondary))
            body.addView(checklistSection(label("DON'Ts", "ಮಾಡಬೇಡಿ"), guide.donts, error))
            body.addView(bigButton(label("Speak instructions", "ಧ್ವನಿಯಲ್ಲಿ ಕೇಳಿ"), "", secondary) {
                speak(guide.speech(language))
            })
        }
    }

    private fun showHospitals() {
        currentScreen = Screen.Hospitals
        setContent(label("Nearby hospitals", "ಹತ್ತಿರದ ಆಸ್ಪತ್ರೆಗಳು"), showBottomNav = true) { body ->
            body.addView(title(label("Offline hospital list", "ಆಫ್‌ಲೈನ್ ಆಸ್ಪತ್ರೆಗಳ ಪಟ್ಟಿ"), label("Use local emergency care when available", "ಲಭ್ಯವಿದ್ದರೆ ಹತ್ತಿರದ ತುರ್ತು ಚಿಕಿತ್ಸೆಯನ್ನು ಬಳಸಿ")))
            hospitals.forEach { hospital ->
                body.addView(infoCard(hospital.name, "${hospital.area}\n${hospital.phone}", secondary) {
                    dial(hospital.phone)
                })
            }
        }
    }

    private fun showProfile() {
        currentScreen = Screen.Profile
        setContent(label("Profile", "ಪ್ರೊಫೈಲ್"), showBottomNav = true) { body ->
            body.addView(title(label("Emergency profile", "ತುರ್ತು ಪ್ರೊಫೈಲ್"), label("Keep this ready before an emergency", "ತುರ್ತು ಪರಿಸ್ಥಿತಿಗೆ ಮೊದಲು ಇದನ್ನು ಸಿದ್ಧವಾಗಿಡಿ")))
            body.addView(infoCard(label("Blood group", "ರಕ್ತ ಗುಂಪು"), "O+", primaryDark))
            body.addView(infoCard(label("Emergency contact", "ತುರ್ತು ಸಂಪರ್ಕ"), "+91 98765 43210", secondary))
            body.addView(infoCard(label("Medical notes", "ವೈದ್ಯಕೀಯ ಟಿಪ್ಪಣಿಗಳು"), label("No known allergies. Edit in the next version.", "ತಿಳಿದಿರುವ ಅಲರ್ಜಿಗಳಿಲ್ಲ. ಮುಂದಿನ ಆವೃತ್ತಿಯಲ್ಲಿ ಸಂಪಾದಿಸಿ."), warning))
        }
    }

    private fun showNearbyHospitals() {
        currentScreen = Screen.Hospitals
        setContent(label("Nearby hospitals", "ಹತ್ತಿರದ ಆಸ್ಪತ್ರೆಗಳು"), showBottomNav = true) { body ->
            body.addView(title(label("Nearby hospitals", "ಹತ್ತಿರದ ಆಸ್ಪತ್ರೆಗಳು"), label("Enable location to fetch live nearby hospitals", "ಲೈವ್ ಹತ್ತಿರದ ಆಸ್ಪತ್ರೆಗಳಿಗಾಗಿ ಸ್ಥಳವನ್ನು ಸಕ್ರಿಯಗೊಳಿಸಿ")))
            if (!hasLocationPermission()) {
                body.addView(infoCard(label("Location is off", "ಸ್ಥಳ ಅನುಮತಿ ಇಲ್ಲ"), label("Allow location permission to list hospitals around you. The fallback list stays available.", "ನಿಮ್ಮ ಸುತ್ತಲಿನ ಆಸ್ಪತ್ರೆಗಳನ್ನು ನೋಡಲು ಸ್ಥಳ ಅನುಮತಿ ನೀಡಿ. ಪರ್ಯಾಯ ಪಟ್ಟಿ ಲಭ್ಯವಿರುತ್ತದೆ."), warning))
                body.addView(bigButton(label("Enable location", "ಸ್ಥಳ ಸಕ್ರಿಯಗೊಳಿಸಿ"), "", secondary) { requestLocationPermission() })
                body.addView(labelView(label("Fallback list", "ಪರ್ಯಾಯ ಪಟ್ಟಿ"), primaryDark, 20f, true))
                fallbackHospitals.forEach { hospital -> body.addView(hospitalCard(hospital)) }
                return@setContent
            }

            body.addView(bigButton(label("Refresh nearby hospitals", "ಹತ್ತಿರದ ಆಸ್ಪತ್ರೆಗಳನ್ನು ಮತ್ತೆ ಹುಡುಕಿ"), "", secondary) {
                onlineHospitals = emptyList()
                hospitalFetchStarted = false
                fetchNearbyHospitals()
                showNearbyHospitals()
            })
            if (hospitalStatus.isNotBlank()) {
                body.addView(infoCard(label("Status", "ಸ್ಥಿತಿ"), hospitalStatus, warning))
            }
            val visibleHospitals = onlineHospitals.ifEmpty { fallbackHospitals }
            if (onlineHospitals.isEmpty()) {
                body.addView(labelView(label("Fallback list", "ಪರ್ಯಾಯ ಪಟ್ಟಿ"), primaryDark, 20f, true))
            }
            visibleHospitals.forEach { hospital -> body.addView(hospitalCard(hospital)) }
            if (!hospitalFetchStarted) fetchNearbyHospitals()
        }
    }

    private fun showEditableProfile() {
        currentScreen = Screen.Profile
        setContent(label("Profile", "ಪ್ರೊಫೈಲ್"), showBottomNav = true) { body ->
            body.addView(title(label("Emergency profile", "ತುರ್ತು ಪ್ರೊಫೈಲ್"), label("Fill basic details responders may need", "ಸಹಾಯಕರಿಗೆ ಬೇಕಾಗುವ ಮೂಲ ಮಾಹಿತಿಯನ್ನು ತುಂಬಿ")))
            val fields = listOf(
                ProfileField("name", label("Full name", "ಪೂರ್ಣ ಹೆಸರು"), android.text.InputType.TYPE_CLASS_TEXT),
                ProfileField("phone", label("Phone number", "ಫೋನ್ ಸಂಖ್ಯೆ"), android.text.InputType.TYPE_CLASS_PHONE),
                ProfileField("address", label("Address", "ವಿಳಾಸ"), android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE),
                ProfileField("blood", label("Blood group", "ರಕ್ತ ಗುಂಪು"), android.text.InputType.TYPE_CLASS_TEXT),
                ProfileField("contact", label("Emergency contact", "ತುರ್ತು ಸಂಪರ್ಕ"), android.text.InputType.TYPE_CLASS_PHONE),
                ProfileField("notes", label("Allergies / medical notes", "ಅಲರ್ಜಿ / ವೈದ್ಯಕೀಯ ಟಿಪ್ಪಣಿಗಳು"), android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE)
            )
            val inputs = fields.associateWith { field ->
                profileInput(field.label, profilePrefs.getString(field.key, "").orEmpty(), field.inputType).also {
                    body.addView(it, matchWrap())
                }
            }
            body.addView(bigButton(label("Save profile", "ಪ್ರೊಫೈಲ್ ಉಳಿಸಿ"), "", primary) {
                val editor = profilePrefs.edit()
                inputs.forEach { (field, input) -> editor.putString(field.key, input.text.toString().trim()) }
                editor.apply()
                Toast.makeText(this, label("Profile saved", "ಪ್ರೊಫೈಲ್ ಉಳಿಸಲಾಗಿದೆ"), Toast.LENGTH_SHORT).show()
                showEditableProfile()
            })
            val emergencyPhone = profilePrefs.getString("contact", "").orEmpty()
            if (emergencyPhone.isNotBlank()) {
                body.addView(bigButton(label("Call emergency contact", "ತುರ್ತು ಸಂಪರ್ಕಕ್ಕೆ ಕರೆ ಮಾಡಿ"), "", secondary) { dial(emergencyPhone) })
            }
        }
    }

    private fun setContent(title: String, showBottomNav: Boolean, content: (LinearLayout) -> Unit) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(surface)
        }
        root.addView(topBar(title))

        val frame = FrameLayout(this)
        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(if (showBottomNav) 156 else 96))
        }
        scroll.addView(body)
        frame.addView(scroll)
        content(body)
        frame.addView(smallFab {
            speak(label("Emergency guide is ready. Choose a topic and follow one step at a time.", "ತುರ್ತು ಮಾರ್ಗದರ್ಶಿ ಸಿದ್ಧವಾಗಿದೆ. ವಿಷಯವನ್ನು ಆಯ್ಕೆಮಾಡಿ ಮತ್ತು ಹಂತ ಹಂತವಾಗಿ ಅನುಸರಿಸಿ."))
        })

        root.addView(frame, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(callBar())
        if (showBottomNav) root.addView(bottomNav())
        setContentView(root)
    }

    private fun topBar(title: String): View {
        return LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), 0, dp(12), 0)
            setBackgroundColor(surface)
            minimumHeight = dp(56)
            addView(iconButton("≡") { showHome() })
            addView(TextView(context).apply {
                text = title
                textSize = 21f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(primaryDark)
                maxLines = 1
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(iconButton("◎") { showEditableProfile() })
        }
    }

    private fun bottomNav(): View {
        return LinearLayout(this).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(6), dp(4), dp(6), dp(4))
            addView(navItem(label("Home", "ಮನೆ"), currentScreen == Screen.Home) { showHome() })
            addView(navItem(label("Hospitals", "ಆಸ್ಪತ್ರೆಗಳು"), currentScreen == Screen.Hospitals) { showNearbyHospitals() })
            addView(navItem(label("Profile", "ಪ್ರೊಫೈಲ್"), currentScreen == Screen.Profile) { showEditableProfile() })
        }
    }

    private fun callBar(): View {
        return Button(this).apply {
            text = "CALL 108"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = rounded(primary, 0)
            minHeight = dp(72)
            setOnClickListener { dial("108") }
        }
    }

    private fun title(main: String, sub: String): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, dp(10), 0, dp(18))
            addView(labelView(main, textColor, 28f, true, Gravity.CENTER))
            addView(labelView(sub, muted, 16f, false, Gravity.CENTER))
        }
    }

    private fun hero(main: String, sub: String): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = roundedStroke(surfaceContainer, Color.rgb(215, 228, 243), 1, 16)
            setPadding(dp(20), dp(24), dp(20), dp(24))
            addView(labelView("+", primary, 64f, true, Gravity.CENTER))
            addView(labelView(main, textColor, 24f, true, Gravity.CENTER))
            addView(labelView(sub, muted, 16f, false, Gravity.CENTER))
        }.also { it.layoutParams = withBottomMargin(matchWrap(), dp(24)) }
    }

    private fun tile(guide: Guide, onClick: () -> Unit): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = roundedStroke(Color.WHITE, guide.color, 2, 8)
            setPadding(dp(8), dp(10), dp(8), dp(10))
            isClickable = true
            setOnClickListener { onClick() }
            addView(labelView(guide.icon, guide.color, 30f, true, Gravity.CENTER))
            addView(labelView(guide.title(language), textColor, 15f, true, Gravity.CENTER))
        }.also {
            it.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = dp(132)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(4), dp(4), dp(4), dp(8))
            }
        }
    }

    private fun guideHeader(guide: Guide): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(guide.color, 12)
            setPadding(dp(16), dp(16), dp(16), dp(16))
            addView(labelView(guide.title(language), Color.WHITE, 26f, true))
            addView(labelView(label("Quick DOs and DON'Ts until help arrives.", "ಸಹಾಯ ಬರುವವರೆಗೆ ತ್ವರಿತ ಮಾಡಿ ಮತ್ತು ಮಾಡಬೇಡಿ ಸೂಚನೆಗಳು."), Color.WHITE, 16f, false))
        }.also { it.layoutParams = withBottomMargin(matchWrap(), dp(16)) }
    }

    private fun checklistSection(title: String, items: List<LocalText>, color: Int): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedStroke(adjustAlpha(color, 0.12f), color, 2, 12)
            setPadding(dp(16), dp(16), dp(16), dp(12))
            addView(labelView(title, color, 22f, true))
            items.forEachIndexed { index, item ->
                addView(checklistItem(index + 1, item.value(language)))
            }
        }.also { it.layoutParams = withBottomMargin(matchWrap(), dp(14)) }
    }

    private fun checklistItem(number: Int, body: String): View {
        return TextView(this).apply {
            text = "$number. $body"
            textSize = 16f
            setTextColor(textColor)
            setPadding(0, dp(10), 0, 0)
            includeFontPadding = true
        }.also { it.layoutParams = withTopMargin(matchWrap(), dp(8)) }
    }

    private fun infoCard(title: String, body: String, color: Int, trailing: View? = null, onClick: (() -> Unit)? = null): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = roundedStroke(card, color, 1, 12)
            setPadding(dp(14), dp(14), dp(14), dp(14))
            isClickable = onClick != null
            if (onClick != null) setOnClickListener { onClick() }
            val copy = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(labelView(title, textColor, 18f, true))
                addView(labelView(body, muted, 15f, false))
            }
            addView(copy, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            trailing?.let { addView(it) }
        }.also { it.layoutParams = withBottomMargin(matchWrap(), dp(14)) }
    }

    private fun bigButton(main: String, sub: String, color: Int, outlined: Boolean = false, onClick: () -> Unit): View {
        return Button(this).apply {
            text = if (sub.isBlank()) main else "$main\n$sub"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (outlined) color else Color.WHITE)
            background = if (outlined) roundedStroke(surface, color, 2, 16) else rounded(color, 16)
            minHeight = dp(64)
            setAllCaps(false)
            setOnClickListener { onClick() }
        }.also { it.layoutParams = withBottomMargin(matchWrap(), dp(14)) }
    }

    private fun smallFab(onClick: () -> Unit): View {
        return Button(this).apply {
            text = "♪"
            textSize = 28f
            setTextColor(Color.WHITE)
            background = rounded(secondary, 999)
            setOnClickListener { onClick() }
        }.also {
            it.layoutParams = FrameLayout.LayoutParams(dp(60), dp(60), Gravity.BOTTOM or Gravity.END).apply {
                setMargins(0, 0, dp(18), dp(18))
            }
        }
    }

    private fun navItem(textValue: String, selected: Boolean, onClick: () -> Unit): View {
        return Button(this).apply {
            text = textValue
            setAllCaps(false)
            textSize = 13f
            setTextColor(if (selected) Color.WHITE else muted)
            background = rounded(if (selected) secondary else Color.TRANSPARENT, 12)
            setOnClickListener { onClick() }
        }.also { it.layoutParams = LinearLayout.LayoutParams(0, dp(56), 1f) }
    }

    private fun iconButton(value: String, onClick: () -> Unit): View {
        return Button(this).apply {
            text = value
            textSize = 24f
            setTextColor(primaryDark)
            background = rounded(Color.TRANSPARENT, 999)
            setOnClickListener { onClick() }
        }.also { it.layoutParams = LinearLayout.LayoutParams(dp(52), dp(52)) }
    }

    private fun labelView(value: String, color: Int, size: Float, bold: Boolean, gravity: Int = Gravity.START): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            this.gravity = gravity
            includeFontPadding = true
        }
    }

    private fun profileInput(hintText: String, value: String, inputTypeValue: Int): EditText {
        return EditText(this).apply {
            hint = hintText
            setText(value)
            inputType = inputTypeValue
            textSize = 18f
            minHeight = dp(if (inputTypeValue and android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE != 0) 96 else 56)
            setTextColor(textColor)
            setHintTextColor(muted)
            background = roundedStroke(card, Color.rgb(141, 112, 108), 1, 12)
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }
    }

    private fun hospitalCard(hospital: Hospital): View {
        val phoneLine = if (hospital.phone.isBlank()) label("Tap to open map", "ನಕ್ಷೆ ತೆರೆಯಲು ಒತ್ತಿ") else hospital.phone
        return infoCard(hospital.name, "${hospital.area}\n$phoneLine", secondary) {
            if (hospital.phone.isNotBlank()) dial(hospital.phone) else openMap(hospital)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_REQUEST_CODE)
    }

    @Suppress("DEPRECATION")
    private fun fetchNearbyHospitals() {
        if (!hasLocationPermission() || hospitalFetchStarted) return
        hospitalFetchStarted = true
        hospitalStatus = label("Getting your location...", "ನಿಮ್ಮ ಸ್ಥಳವನ್ನು ಹುಡುಕಲಾಗುತ್ತಿದೆ...")
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val lastLocation = getBestKnownLocation(locationManager)
        if (lastLocation != null) {
            fetchHospitalsForLocation(lastLocation)
            return
        }

        val provider = when {
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            else -> null
        }
        if (provider == null) {
            hospitalStatus = label("Turn on device location to fetch nearby hospitals.", "ಹತ್ತಿರದ ಆಸ್ಪತ್ರೆಗಳನ್ನು ಪಡೆಯಲು ಸಾಧನದ ಸ್ಥಳ ಸೇವೆಯನ್ನು ಆನ್ ಮಾಡಿ.")
            hospitalFetchStarted = false
            showNearbyHospitals()
            return
        }

        try {
            locationManager.requestSingleUpdate(provider, object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    fetchHospitalsForLocation(location)
                }

                override fun onProviderDisabled(provider: String) {
                    hospitalStatus = label("Location provider is disabled.", "ಸ್ಥಳ ಸೇವೆ ನಿಷ್ಕ್ರಿಯವಾಗಿದೆ.")
                    hospitalFetchStarted = false
                    showNearbyHospitals()
                }
            }, Looper.getMainLooper())
        } catch (securityException: SecurityException) {
            hospitalStatus = label("Location permission was not granted.", "ಸ್ಥಳ ಅನುಮತಿ ನೀಡಲಾಗಿಲ್ಲ.")
            hospitalFetchStarted = false
            showNearbyHospitals()
        }
    }

    private fun getBestKnownLocation(locationManager: LocationManager): Location? {
        return try {
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            providers.mapNotNull { provider ->
                if (locationManager.isProviderEnabled(provider)) locationManager.getLastKnownLocation(provider) else null
            }.maxByOrNull { it.time }
        } catch (securityException: SecurityException) {
            null
        }
    }

    private fun fetchHospitalsForLocation(location: Location) {
        hospitalStatus = label("Searching online hospital list...", "ಆನ್‌ಲೈನ್ ಆಸ್ಪತ್ರೆ ಪಟ್ಟಿಯನ್ನು ಹುಡುಕಲಾಗುತ್ತಿದೆ...")
        Thread {
            val result = runCatching { loadHospitalsFromOverpass(location.latitude, location.longitude) }
            mainHandler.post {
                result.onSuccess { hospitals ->
                    onlineHospitals = hospitals
                    hospitalStatus = if (hospitals.isEmpty()) {
                        label("No online hospitals found nearby. Showing fallback list.", "ಹತ್ತಿರದಲ್ಲಿ ಆನ್‌ಲೈನ್ ಆಸ್ಪತ್ರೆಗಳು ಸಿಗಲಿಲ್ಲ. ಪರ್ಯಾಯ ಪಟ್ಟಿ ತೋರಿಸಲಾಗುತ್ತಿದೆ.")
                    } else {
                        label("Found ${hospitals.size} nearby hospitals online.", "ಆನ್‌ಲೈನ್‌ನಲ್ಲಿ ${hospitals.size} ಹತ್ತಿರದ ಆಸ್ಪತ್ರೆಗಳು ಸಿಕ್ಕಿವೆ.")
                    }
                }.onFailure {
                    onlineHospitals = emptyList()
                    hospitalStatus = label("Could not fetch online hospitals. Showing fallback list.", "ಆನ್‌ಲೈನ್ ಆಸ್ಪತ್ರೆಗಳನ್ನು ಪಡೆಯಲಾಗಲಿಲ್ಲ. ಪರ್ಯಾಯ ಪಟ್ಟಿ ತೋರಿಸಲಾಗುತ್ತಿದೆ.")
                }
                showNearbyHospitals()
            }
        }.start()
    }

    private fun loadHospitalsFromOverpass(latitude: Double, longitude: Double): List<Hospital> {
        val query = """
            [out:json][timeout:20];
            (
              node["amenity"="hospital"](around:7000,$latitude,$longitude);
              way["amenity"="hospital"](around:7000,$latitude,$longitude);
              relation["amenity"="hospital"](around:7000,$latitude,$longitude);
            );
            out center tags 30;
        """.trimIndent()
        val connection = (URL("https://overpass-api.de/api/interpreter").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 20000
            doOutput = true
        }
        OutputStreamWriter(connection.outputStream).use { it.write(query) }
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val elements = JSONObject(response).getJSONArray("elements")
        val hospitals = mutableListOf<Hospital>()
        for (index in 0 until elements.length()) {
            val element = elements.getJSONObject(index)
            val tags = element.optJSONObject("tags") ?: continue
            val name = tags.optString("name")
            if (name.isBlank()) continue
            val lat = if (element.has("lat")) element.optDouble("lat") else element.optJSONObject("center")?.optDouble("lat") ?: latitude
            val lon = if (element.has("lon")) element.optDouble("lon") else element.optJSONObject("center")?.optDouble("lon") ?: longitude
            val address = listOf(
                tags.optString("addr:housenumber"),
                tags.optString("addr:street"),
                tags.optString("addr:city")
            ).filter { it.isNotBlank() }.joinToString(", ")
            val distance = distanceKm(latitude, longitude, lat, lon)
            val area = "${"%.1f".format(Locale.US, distance)} km away" + if (address.isBlank()) "" else "\n$address"
            val phone = tags.optString("phone").ifBlank { tags.optString("contact:phone") }
            hospitals.add(Hospital(name, area, phone))
        }
        connection.disconnect()
        return hospitals.distinctBy { it.name.lowercase(Locale.US) }.take(20)
    }

    private fun distanceKm(startLat: Double, startLon: Double, endLat: Double, endLon: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(endLat - startLat)
        val dLon = Math.toRadians(endLon - startLon)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(startLat)) * cos(Math.toRadians(endLat)) *
            sin(dLon / 2) * sin(dLon / 2)
        return earthRadius * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun openMap(hospital: Hospital) {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode("${hospital.name} ${hospital.area}")}")
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun speak(message: String) {
        tts?.language = if (language == AppLanguage.KANNADA) Locale("kn", "IN") else Locale.US
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "first-aid-guide")
    }

    private fun dial(number: String) {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun roundedStroke(color: Int, stroke: Int, width: Int, radius: Int): GradientDrawable {
        return rounded(color, radius).apply { setStroke(dp(width), stroke) }
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        return Color.argb((255 * factor).toInt(), Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun matchWrap(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun withBottomMargin(params: LinearLayout.LayoutParams, margin: Int): LinearLayout.LayoutParams {
        params.setMargins(0, 0, 0, margin)
        return params
    }

    private fun withTopMargin(params: LinearLayout.LayoutParams, margin: Int): LinearLayout.LayoutParams {
        params.setMargins(0, margin, 0, 0)
        return params
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun label(en: String, kn: String): String = if (language == AppLanguage.ENGLISH) en else kn

    private fun guide(
        id: String,
        titleEn: String,
        titleKn: String,
        icon: String,
        color: Int,
        dos: List<Pair<String, String>>,
        donts: List<Pair<String, String>>
    ): Guide {
        return Guide(id, titleEn, titleKn, icon, color, dos.map { LocalText(it.first, it.second) }, donts.map { LocalText(it.first, it.second) })
    }

    private val guides: List<Guide>
        get() = listOf(
            guide("choking", "Choking", "ಉಸಿರುಗಟ್ಟುವಿಕೆ", "!", primary,
                dos = listOf(
                    "Ask \"Are you choking?\" If they can't speak or cough, act immediately." to "ನಿಮಗೆ ಉಸಿರುಗಟ್ಟುತ್ತಿದೆಯೇ ಎಂದು ಕೇಳಿ. ಮಾತನಾಡಲು ಅಥವಾ ಕೆಮ್ಮಲು ಆಗದಿದ್ದರೆ ತಕ್ಷಣ ಕ್ರಮ ಕೈಗೊಳ್ಳಿ.",
                    "Give 5 firm back blows between the shoulder blades." to "ಭುಜಗಳ ನಡುವೆ 5 ಗಟ್ಟಿಯಾದ ಬೆನ್ನಿನ ಹೊಡೆತಗಳನ್ನು ನೀಡಿ.",
                    "Follow with 5 abdominal thrusts (Heimlich manoeuvre)." to "ನಂತರ 5 ಹೊಟ್ಟೆಯ ಒತ್ತಡಗಳನ್ನು (ಹೈಮ್ಲಿಕ್ ವಿಧಾನ) ನೀಡಿ.",
                    "Alternate back blows and abdominal thrusts until the object is expelled." to "ವಸ್ತು ಹೊರಬರುವವರೆಗೆ ಬೆನ್ನಿನ ಹೊಡೆತಗಳು ಮತ್ತು ಹೊಟ್ಟೆಯ ಒತ್ತಡಗಳನ್ನು ಪರ್ಯಾಯವಾಗಿ ಮಾಡಿ.",
                    "Call 108 if unconscious or the object doesn't clear." to "ಪ್ರಜ್ಞೆ ಕಳೆದುಕೊಂಡರೆ ಅಥವಾ ವಸ್ತು ತೆರವಾಗದಿದ್ದರೆ 108 ಕರೆ ಮಾಡಿ."
                ),
                donts = listOf(
                    "Don't do blind finger sweeps in the mouth." to "ಬಾಯಿಯಲ್ಲಿ ಕಾಣದೆ ಬೆರಳು ಹಾಕಿ ತೆಗೆಯಲು ಪ್ರಯತ್ನಿಸಬೇಡಿ.",
                    "Don't give water to wash it down." to "ವಸ್ತು ಇಳಿಯಲು ನೀರು ಕೊಡಬೇಡಿ.",
                    "Don't leave the person alone." to "ವ್ಯಕ್ತಿಯನ್ನು ಒಬ್ಬರೇ ಬಿಡಬೇಡಿ.",
                    "Don't perform abdominal thrusts on infants; use back blows and chest thrusts instead." to "ಶಿಶುಗಳಿಗೆ ಹೊಟ್ಟೆಯ ಒತ್ತಡ ಕೊಡಬೇಡಿ; ಬದಲಿಗೆ ಬೆನ್ನಿನ ಹೊಡೆತ ಮತ್ತು ಎದೆ ಒತ್ತಡ ಬಳಸಿ."
                )
            ),
            guide("burns", "Burns - minor and major", "ಸುಟ್ಟ ಗಾಯ", "F", warning,
                dos = listOf(
                    "Cool the burn with running cool water for at least 10-20 minutes." to "ಸುಟ್ಟ ಭಾಗವನ್ನು ಕನಿಷ್ಠ 10-20 ನಿಮಿಷ ತಂಪಾದ ಹರಿಯುವ ನೀರಿನಿಂದ ತಂಪಾಗಿಸಿ.",
                    "Remove jewellery or clothing near the burn if it is not stuck to skin." to "ಚರ್ಮಕ್ಕೆ ಅಂಟಿರದಿದ್ದರೆ ಸುಟ್ಟ ಭಾಗದ ಹತ್ತಿರದ ಆಭರಣ ಅಥವಾ ಬಟ್ಟೆ ತೆಗೆದುಹಾಕಿ.",
                    "Cover loosely with a clean, non-fluffy cloth or cling film." to "ಸ್ವಚ್ಛ, ನಾರು ಬಾರದ ಬಟ್ಟೆ ಅಥವಾ ಕ್ಲಿಂಗ್ ಫಿಲ್ಮ್‌ನಿಂದ ಸಡಿಲವಾಗಿ ಮುಚ್ಚಿ.",
                    "Call 108 for burns larger than the patient's palm, or burns on face, hands, or groin." to "ರೋಗಿಯ ಕರಗಿಂತ ದೊಡ್ಡ ಸುಟ್ಟ ಗಾಯ ಅಥವಾ ಮುಖ, ಕೈ, ಗುಪ್ತಾಂಗದ ಗಾಯಕ್ಕೆ 108 ಕರೆ ಮಾಡಿ."
                ),
                donts = listOf(
                    "Don't apply ice, butter, toothpaste, or oil." to "ಐಸ್, ಬೆಣ್ಣೆ, ಟೂತ್‌ಪೇಸ್ಟ್ ಅಥವಾ ಎಣ್ಣೆ ಹಾಕಬೇಡಿ.",
                    "Don't burst blisters." to "ಗುಳ್ಳೆಗಳನ್ನು ಒಡೆಬೇಡಿ.",
                    "Don't remove clothing stuck to the burn." to "ಸುಟ್ಟ ಗಾಯಕ್ಕೆ ಅಂಟಿದ ಬಟ್ಟೆ ತೆಗೆದುಹಾಕಬೇಡಿ.",
                    "Don't use cotton wool directly on the wound." to "ಗಾಯದ ಮೇಲೆ ನೇರವಾಗಿ ಹತ್ತಿ ಬಳಸಬೇಡಿ."
                )
            ),
            guide("snake", "Snake bite", "ಹಾವು ಕಡಿತ", "S", error,
                dos = listOf(
                    "Keep the victim calm and as still as possible." to "ಬಾಧಿತ ವ್ಯಕ್ತಿಯನ್ನು ಶಾಂತವಾಗಿ ಮತ್ತು ಸಾಧ್ಯವಾದಷ್ಟು ಚಲಿಸದಂತೆ ಇಡಿ.",
                    "Immobilise the bitten limb below heart level." to "ಕಚ್ಚಿದ ಅಂಗವನ್ನು ಹೃದಯದ ಮಟ್ಟಕ್ಕಿಂತ ಕೆಳಗೆ ಚಲಿಸದಂತೆ ಇಡಿ.",
                    "Remove watches, rings, and tight clothing near the bite." to "ಕಚ್ಚಿದ ಭಾಗದ ಹತ್ತಿರದ ಗಡಿಯಾರ, ಉಂಗುರ ಮತ್ತು ಬಿಗಿ ಬಟ್ಟೆ ತೆಗೆದುಹಾಕಿ.",
                    "Note the snake's appearance, but do not chase it." to "ಹಾವಿನ ರೂಪವನ್ನು ಗಮನಿಸಿ, ಆದರೆ ಹಾವನ್ನು ಬೆನ್ನಟ್ಟಬೇಡಿ.",
                    "Rush to hospital immediately; anti-venom is the only cure." to "ತಕ್ಷಣ ಆಸ್ಪತ್ರೆಗೆ ಕರೆದೊಯ್ಯಿರಿ; ಆಂಟಿ-ವೆನಮ್ ಮಾತ್ರ ಚಿಕಿತ್ಸೆ."
                ),
                donts = listOf(
                    "Don't cut, suck, or squeeze the wound." to "ಗಾಯವನ್ನು ಕತ್ತರಿಸಬೇಡಿ, ಹೀರಬೇಡಿ ಅಥವಾ ಒತ್ತಬೇಡಿ.",
                    "Don't apply a tourniquet or tie tightly." to "ಟೂರ್ನಿಕೇಟ್ ಹಾಕಬೇಡಿ ಅಥವಾ ಬಿಗಿಯಾಗಿ ಕಟ್ಟಬೇಡಿ.",
                    "Don't apply ice or immerse in cold water." to "ಐಸ್ ಹಾಕಬೇಡಿ ಅಥವಾ ತಂಪು ನೀರಿನಲ್ಲಿ ಮುಳುಗಿಸಬೇಡಿ.",
                    "Don't give alcohol, painkillers, or home remedies." to "ಮದ್ಯ, ನೋವು ನಿವಾರಕ ಅಥವಾ ಮನೆಮದ್ದು ಕೊಡಬೇಡಿ.",
                    "Don't let the patient walk; carry them." to "ರೋಗಿಯನ್ನು ನಡೆಯಲು ಬಿಡಬೇಡಿ; ಅವರನ್ನು ಎತ್ತಿಕೊಂಡು ಕರೆದೊಯ್ಯಿರಿ."
                )
            ),
            guide("heart", "Heart attack / Cardiac arrest", "ಹೃದಯ ಸ್ತಂಭನ", "+", primary,
                dos = listOf(
                    "Call 108 immediately." to "ತಕ್ಷಣ 108 ಕರೆ ಮಾಡಿ.",
                    "Make the person sit or lie in a comfortable position." to "ವ್ಯಕ್ತಿಯನ್ನು ಆರಾಮದಾಯಕವಾಗಿ ಕುಳ್ಳಿರಿಸಿ ಅಥವಾ ಮಲಗಿಸಿ.",
                    "Loosen tight clothing around chest and neck." to "ಛಾತಿ ಮತ್ತು ಕುತ್ತಿಗೆಯ ಸುತ್ತಲಿನ ಬಿಗಿ ಬಟ್ಟೆ ಸಡಿಲಿಸಿ.",
                    "If unconscious and not breathing, begin CPR: 30 chest compressions and 2 rescue breaths." to "ಪ್ರಜ್ಞೆ ಇಲ್ಲದೆ ಉಸಿರಾಡದಿದ್ದರೆ CPR ಪ್ರಾರಂಭಿಸಿ: 30 ಎದೆ ಒತ್ತಡ ಮತ್ತು 2 ರಕ್ಷಣಾ ಉಸಿರು.",
                    "Continue CPR until help arrives or the person responds." to "ಸಹಾಯ ಬರುವವರೆಗೆ ಅಥವಾ ವ್ಯಕ್ತಿ ಪ್ರತಿಕ್ರಿಯಿಸುವವರೆಗೆ CPR ಮುಂದುವರಿಸಿ."
                ),
                donts = listOf(
                    "Don't give food, water, or any medication unless prescribed." to "ವೈದ್ಯರು ಸೂಚಿಸದಿದ್ದರೆ ಆಹಾರ, ನೀರು ಅಥವಾ ಔಷಧಿ ಕೊಡಬೇಡಿ.",
                    "Don't leave the person alone." to "ವ್ಯಕ್ತಿಯನ್ನು ಒಬ್ಬರೇ ಬಿಡಬೇಡಿ.",
                    "Don't let them walk or exert themselves." to "ಅವರಿಗೆ ನಡೆಯಲು ಅಥವಾ ಶ್ರಮಿಸಲು ಬಿಡಬೇಡಿ.",
                    "Don't wait to see if symptoms pass; act immediately." to "ಲಕ್ಷಣಗಳು ಕಡಿಮೆಯಾಗುತ್ತವೆಯೇ ಎಂದು ಕಾಯಬೇಡಿ; ತಕ್ಷಣ ಕ್ರಮ ಕೈಗೊಳ್ಳಿ."
                )
            ),
            guide("fracture", "Fracture / Broken bone", "ಮೂಳೆ ಮುರಿತ", "/", warning,
                dos = listOf(
                    "Immobilise the injured area using a splint such as a stick or rolled newspaper." to "ಕಡ್ಡಿ ಅಥವಾ ಸುತ್ತಿದ ಪತ್ರಿಕೆ ಮುಂತಾದ ಸ್ಪ್ಲಿಂಟ್ ಬಳಸಿ ಗಾಯವಾದ ಭಾಗವನ್ನು ಚಲಿಸದಂತೆ ಇಡಿ.",
                    "Support the limb in the position found; do not try to straighten." to "ಅಂಗವನ್ನು ಕಂಡ ಸ್ಥಿತಿಯಲ್ಲೇ ಬೆಂಬಲಿಸಿ; ನೇರಗೊಳಿಸಲು ಪ್ರಯತ್ನಿಸಬೇಡಿ.",
                    "Apply a cold pack wrapped in cloth to reduce swelling." to "ಊತ ಕಡಿಮೆ ಮಾಡಲು ಬಟ್ಟೆಯಲ್ಲಿ ಸುತ್ತಿದ ತಂಪು ಪ್ಯಾಕ್ ಇಡಿ.",
                    "Elevate the limb if possible." to "ಸಾಧ್ಯವಾದರೆ ಅಂಗವನ್ನು ಎತ್ತರದಲ್ಲಿಡಿ.",
                    "Call 108 for open fractures, spine, or pelvic injuries." to "ತೆರೆದ ಮುರಿತ, ಬೆನ್ನುಹುರಿ ಅಥವಾ ಸೊಂಟದ ಗಾಯಗಳಿಗೆ 108 ಕರೆ ಮಾಡಿ."
                ),
                donts = listOf(
                    "Don't try to realign or push bones back." to "ಮೂಳೆಯನ್ನು ಸರಿಪಡಿಸಲು ಅಥವಾ ಒಳಗೆ ತಳ್ಳಲು ಪ್ರಯತ್ನಿಸಬೇಡಿ.",
                    "Don't move a person with suspected spine or neck injury." to "ಬೆನ್ನುಹುರಿ ಅಥವಾ ಕುತ್ತಿಗೆ ಗಾಯ ಶಂಕೆಯಿದ್ದರೆ ವ್ಯಕ್ತಿಯನ್ನು ಚಲಿಸಬೇಡಿ.",
                    "Don't apply heat." to "ಬಿಸಿ ಹಾಕಬೇಡಿ.",
                    "Don't give food or water if surgery may be needed." to "ಶಸ್ತ್ರಚಿಕಿತ್ಸೆ ಬೇಕಾಗಬಹುದು ಎಂದರೆ ಆಹಾರ ಅಥವಾ ನೀರು ಕೊಡಬೇಡಿ."
                )
            ),
            guide("bleeding", "Severe bleeding", "ತೀವ್ರ ರಕ್ತಸ್ರಾವ", "B", primary,
                dos = listOf(
                    "Apply firm, direct pressure with a clean cloth continuously." to "ಸ್ವಚ್ಛ ಬಟ್ಟೆಯಿಂದ ನಿರಂತರವಾಗಿ ನೇರ, ಗಟ್ಟಿಯಾದ ಒತ್ತಡ ಹಾಕಿ.",
                    "Elevate the bleeding limb above heart level." to "ರಕ್ತಸ್ರಾವವಾಗುತ್ತಿರುವ ಅಂಗವನ್ನು ಹೃದಯದ ಮಟ್ಟಕ್ಕಿಂತ ಎತ್ತರದಲ್ಲಿಡಿ.",
                    "Add more cloth on top if blood soaks through; don't remove the first layer." to "ರಕ್ತ ಹೊರಬಂದರೆ ಮೇಲಿಂದ ಮತ್ತಷ್ಟು ಬಟ್ಟೆ ಹಾಕಿ; ಮೊದಲ ಪದರ ತೆಗೆದುಹಾಕಬೇಡಿ.",
                    "Call 108 for uncontrolled or arterial bleeding." to "ನಿಯಂತ್ರಣವಾಗದ ಅಥವಾ ಧಮನಿಯ ರಕ್ತಸ್ರಾವಕ್ಕೆ 108 ಕರೆ ಮಾಡಿ."
                ),
                donts = listOf(
                    "Don't remove the cloth to check; it disrupts clotting." to "ಪರಿಶೀಲಿಸಲು ಬಟ್ಟೆ ತೆಗೆದುಹಾಕಬೇಡಿ; ಅದು ರಕ್ತ ಹೆಪ್ಪುಗಟ್ಟುವಿಕೆಗೆ ಅಡ್ಡಿಯಾಗುತ್ತದೆ.",
                    "Don't apply a tourniquet unless trained and bleeding is life-threatening." to "ತರಬೇತಿ ಇಲ್ಲದಿದ್ದರೆ ಮತ್ತು ಜೀವಕ್ಕೆ ಅಪಾಯಕಾರಿಯಲ್ಲದಿದ್ದರೆ ಟೂರ್ನಿಕೇಟ್ ಹಾಕಬೇಡಿ.",
                    "Don't use dirty cloth or cotton wool." to "ಅಶುದ್ಧ ಬಟ್ಟೆ ಅಥವಾ ಹತ್ತಿ ಬಳಸಬೇಡಿ.",
                    "Don't leave the patient unattended." to "ರೋಗಿಯನ್ನು ಗಮನಿಸದೆ ಬಿಡಬೇಡಿ."
                )
            ),
            guide("drowning", "Drowning", "ಮುಳುಗುವಿಕೆ", "D", primary,
                dos = listOf(
                    "Call 108 immediately." to "ತಕ್ಷಣ 108 ಕರೆ ಮಾಡಿ.",
                    "Get the person out of water safely; use rope or a branch and avoid direct contact if possible." to "ಕಡ್ಡಿ ಅಥವಾ ಕಯಿರನ್ನು ಬಳಸಿ ವ್ಯಕ್ತಿಯನ್ನು ಸುರಕ್ಷಿತವಾಗಿ ನೀರಿನಿಂದ ಹೊರತೆಗೆಡಿ; ಸಾಧ್ಯವಾದರೆ ನೇರ ಸಂಪರ್ಕ ತಪ್ಪಿಸಿ.",
                    "Begin CPR immediately if not breathing, using the 30:2 ratio." to "ಉಸಿರಾಡದಿದ್ದರೆ ತಕ್ಷಣ 30:2 ಅನುಪಾತದಲ್ಲಿ CPR ಪ್ರಾರಂಭಿಸಿ.",
                    "Keep the person warm; cover with a blanket." to "ವ್ಯಕ್ತಿಯನ್ನು ಬೆಚ್ಚಗಿಡಿ; ಹಾಸಿಗೆ ಅಥವಾ ಕಂಬಳಿಯಿಂದ ಮುಚ್ಚಿ.",
                    "Place in recovery position if breathing." to "ಉಸಿರಾಡುತ್ತಿದ್ದರೆ ಚೇತರಿಕೆ ಸ್ಥಿತಿಯಲ್ಲಿ ಮಲಗಿಸಿ."
                ),
                donts = listOf(
                    "Don't try to drain water by holding upside down; it wastes critical CPR time." to "ತಲೆಕೆಳಗಾಗಿ ಹಿಡಿದು ನೀರು ಹೊರಹಾಕಲು ಪ್ರಯತ್ನಿಸಬೇಡಿ; ಇದು ಅಮೂಲ್ಯ CPR ಸಮಯ ವ್ಯರ್ಥ ಮಾಡುತ್ತದೆ.",
                    "Don't enter fast-moving or deep water unless trained." to "ತರಬೇತಿ ಇಲ್ಲದಿದ್ದರೆ ವೇಗದ ಅಥವಾ ಆಳವಾದ ನೀರಿಗೆ ಇಳಿಯಬೇಡಿ.",
                    "Don't leave the person alone even if they seem fine." to "ಸರಿಯಾಗಿ ಕಾಣಿಸಿದರೂ ವ್ಯಕ್ತಿಯನ್ನು ಒಬ್ಬರೇ ಬಿಡಬೇಡಿ.",
                    "Don't give food or water until fully conscious." to "ಪೂರ್ಣ ಪ್ರಜ್ಞೆ ಬರುವವರೆಗೆ ಆಹಾರ ಅಥವಾ ನೀರು ಕೊಡಬೇಡಿ."
                )
            ),
            guide("seizure", "Seizure / Epilepsy", "ಅಪಸ್ಮಾರ", "*", secondary,
                dos = listOf(
                    "Keep the person safe; clear hard or sharp objects around them." to "ವ್ಯಕ್ತಿಯನ್ನು ಸುರಕ್ಷಿತವಾಗಿಡಿ; ಸುತ್ತಲಿನ ಕಠಿಣ ಅಥವಾ ತೀಕ್ಷ್ಣ ವಸ್ತುಗಳನ್ನು ದೂರಮಾಡಿ.",
                    "Time the seizure duration." to "ಅಪಸ್ಮಾರದ ಅವಧಿಯನ್ನು ಸಮಯ ಮಾಡಿ.",
                    "Place something soft under the head." to "ತಲೆಯ ಕೆಳಗೆ ಮೃದುವಾದದ್ದನ್ನು ಇಡಿ.",
                    "After seizure stops, turn them into recovery position." to "ಅಪಸ್ಮಾರ ನಿಂತ ನಂತರ ಅವರನ್ನು ಚೇತರಿಕೆ ಸ್ಥಿತಿಗೆ ತಿರುಗಿಸಿ.",
                    "Stay until fully conscious; call 108 if seizure lasts more than 5 minutes." to "ಪೂರ್ಣ ಪ್ರಜ್ಞೆ ಬರುವವರೆಗೆ ಜೊತೆಯಲ್ಲಿರಿ; 5 ನಿಮಿಷಕ್ಕಿಂತ ಹೆಚ್ಚು ಮುಂದುವರಿದರೆ 108 ಕರೆ ಮಾಡಿ."
                ),
                donts = listOf(
                    "Don't restrain or hold the person down." to "ವ್ಯಕ್ತಿಯನ್ನು ಬಲವಂತವಾಗಿ ಹಿಡಿಯಬೇಡಿ.",
                    "Don't put anything in the mouth: not a finger, spoon, or cloth." to "ಬಾಯಿಗೆ ಏನನ್ನೂ ಹಾಕಬೇಡಿ: ಬೆರಳು, ಚಮಚ ಅಥವಾ ಬಟ್ಟೆ ಕೂಡ ಬೇಡ.",
                    "Don't give water until fully alert." to "ಪೂರ್ಣ ಎಚ್ಚರ ಬರುವವರೆಗೆ ನೀರು ಕೊಡಬೇಡಿ.",
                    "Don't leave the person alone." to "ವ್ಯಕ್ತಿಯನ್ನು ಒಬ್ಬರೇ ಬಿಡಬೇಡಿ."
                )
            ),
            guide("electrocution", "Electrocution", "ವಿದ್ಯುತ್ ಆಘಾತ", "E", primary,
                dos = listOf(
                    "Switch off the power source at the mains first." to "ಮೊದಲಿಗೆ ಮುಖ್ಯ ವಿದ್ಯುತ್ ಮೂಲವನ್ನು ಆಫ್ ಮಾಡಿ.",
                    "Use a dry wooden stick or rubber object to move the person away from the source." to "ಒಣ ಮರದ ಕಡ್ಡಿ ಅಥವಾ ರಬ್ಬರ್ ವಸ್ತುವಿನಿಂದ ವ್ಯಕ್ತಿಯನ್ನು ಮೂಲದಿಂದ ದೂರಮಾಡಿ.",
                    "Check breathing and begin CPR if absent." to "ಉಸಿರಾಟ ಪರಿಶೀಲಿಸಿ; ಇಲ್ಲದಿದ್ದರೆ CPR ಪ್ರಾರಂಭಿಸಿ.",
                    "Treat visible burns with cool running water." to "ಕಾಣುವ ಸುಟ್ಟ ಗಾಯಗಳನ್ನು ತಂಪಾದ ಹರಿಯುವ ನೀರಿನಿಂದ ತಂಪಾಗಿಸಿ.",
                    "Call 108; all electrocution cases need hospital evaluation." to "108 ಕರೆ ಮಾಡಿ; ಎಲ್ಲಾ ವಿದ್ಯುತ್ ಆಘಾತಗಳಿಗೆ ಆಸ್ಪತ್ರೆ ಪರಿಶೀಲನೆ ಅಗತ್ಯ."
                ),
                donts = listOf(
                    "Don't touch the person while still in contact with the electrical source." to "ವಿದ್ಯುತ್ ಮೂಲದ ಸಂಪರ್ಕದಲ್ಲಿರುವಾಗ ವ್ಯಕ್ತಿಯನ್ನು ಮುಟ್ಟಬೇಡಿ.",
                    "Don't use wet or metal objects." to "ಒದ್ದೆ ಅಥವಾ ಲೋಹದ ವಸ್ತುಗಳನ್ನು ಬಳಸಬೇಡಿ.",
                    "Don't assume they are fine because burns look minor." to "ಸುಟ್ಟ ಗಾಯ ಸಣ್ಣದಾಗಿ ಕಾಣುತ್ತದೆ ಎಂದು ಅವರು ಚೆನ್ನಾಗಿದ್ದಾರೆಂದು ಊಹಿಸಬೇಡಿ.",
                    "Don't give food or water immediately." to "ತಕ್ಷಣ ಆಹಾರ ಅಥವಾ ನೀರು ಕೊಡಬೇಡಿ."
                )
            ),
            guide("eye", "Eye injury", "ಕಣ್ಣಿನ ಗಾಯ", "I", warning,
                dos = listOf(
                    "For chemical splash, flush with clean running water for 15-20 minutes." to "ರಾಸಾಯನಿಕ ತಗುಲಿದರೆ 15-20 ನಿಮಿಷ ಸ್ವಚ್ಛ ಹರಿಯುವ ನೀರಿನಿಂದ ತೊಳೆಯಿರಿ.",
                    "Keep the eye gently open during washing." to "ತೊಳೆಯುವಾಗ ಕಣ್ಣನ್ನು ನಿಧಾನವಾಗಿ ತೆರೆದಿಡಿ.",
                    "Cover the eye loosely with a clean pad." to "ಕಣ್ಣನ್ನು ಸ್ವಚ್ಛ ಪ್ಯಾಡ್‌ನಿಂದ ಸಡಿಲವಾಗಿ ಮುಚ್ಚಿ.",
                    "Seek hospital care immediately for any eye injury." to "ಯಾವುದೇ ಕಣ್ಣಿನ ಗಾಯಕ್ಕೆ ತಕ್ಷಣ ಆಸ್ಪತ್ರೆ ಚಿಕಿತ್ಸೆ ಪಡೆಯಿರಿ.",
                    "For embedded objects, cover both eyes to prevent movement." to "ಒಳಗೆ ಸಿಲುಕಿದ ವಸ್ತುಗಳಿದ್ದರೆ ಚಲನೆ ತಪ್ಪಿಸಲು ಎರಡೂ ಕಣ್ಣುಗಳನ್ನು ಮುಚ್ಚಿ."
                ),
                donts = listOf(
                    "Don't rub the eye." to "ಕಣ್ಣನ್ನು ಒರೆಸಬೇಡಿ.",
                    "Don't try to remove embedded objects." to "ಒಳಗೆ ಸಿಲುಕಿದ ವಸ್ತುಗಳನ್ನು ತೆಗೆದುಹಾಕಲು ಪ್ರಯತ್ನಿಸಬೇಡಿ.",
                    "Don't apply eye drops unless prescribed." to "ವೈದ್ಯರು ಸೂಚಿಸದಿದ್ದರೆ ಕಣ್ಣಿನ ಹನಿಗಳನ್ನು ಹಾಕಬೇಡಿ.",
                    "Don't patch tightly; it increases pressure." to "ಬಿಗಿಯಾಗಿ ಮುಚ್ಚಬೇಡಿ; ಅದು ಒತ್ತಡ ಹೆಚ್ಚಿಸುತ್ತದೆ."
                )
            ),
            guide("heat", "Heat stroke", "ಶಾಖ ಆಘಾತ", "H", warning,
                dos = listOf(
                    "Move person to shade or a cool area immediately." to "ವ್ಯಕ್ತಿಯನ್ನು ತಕ್ಷಣ ನೆರಳು ಅಥವಾ ತಂಪಾದ ಸ್ಥಳಕ್ಕೆ ಕರೆದೊಯ್ಯಿರಿ.",
                    "Remove excess clothing." to "ಅತಿಯಾದ ಬಟ್ಟೆ ತೆಗೆದುಹಾಕಿ.",
                    "Cool rapidly with wet cloth on neck, armpits, and groin; fan them." to "ಕುತ್ತಿಗೆ, ಕೈಕುಳಿ ಮತ್ತು ತೊಡೆಯ ಒಳಭಾಗಕ್ಕೆ ಒದ್ದೆ ಬಟ್ಟೆ ಇಟ್ಟು ವೇಗವಾಗಿ ತಂಪಾಗಿಸಿ; ಗಾಳಿ ಬೀಸಿ.",
                    "Give cool water to sip if conscious and able to swallow." to "ಪ್ರಜ್ಞೆಯಿದ್ದು ನುಂಗಲು ಸಾಧ್ಯವಾದರೆ ತಂಪು ನೀರನ್ನು ಸ್ವಲ್ಪ ಸ್ವಲ್ಪವಾಗಿ ಕೊಡಿರಿ.",
                    "Call 108; heat stroke is life-threatening." to "108 ಕರೆ ಮಾಡಿ; ಶಾಖ ಆಘಾತ ಜೀವಕ್ಕೆ ಅಪಾಯಕಾರಿಯಾಗಿದೆ."
                ),
                donts = listOf(
                    "Don't give very cold or iced water." to "ತುಂಬಾ ತಂಪಾದ ಅಥವಾ ಐಸ್ ನೀರು ಕೊಡಬೇಡಿ.",
                    "Don't leave in the sun." to "ಬಿಸಿಲಿನಲ್ಲಿ ಬಿಡಬೇಡಿ.",
                    "Don't give food, tea, or coffee." to "ಆಹಾರ, ಚಹಾ ಅಥವಾ ಕಾಫಿ ಕೊಡಬೇಡಿ.",
                    "Don't ignore confusion or loss of consciousness." to "ಗೊಂದಲ ಅಥವಾ ಪ್ರಜ್ಞೆ ಕಳೆದುಕೊಳ್ಳುವುದನ್ನು ನಿರ್ಲಕ್ಷಿಸಬೇಡಿ."
                )
            ),
            guide("poisoning", "Poisoning / Ingestion", "ವಿಷ ಸೇವನೆ", "P", error,
                dos = listOf(
                    "Identify the substance and keep the container or packet." to "ಯಾವ ಪದಾರ್ಥವೆಂದು ಗುರುತಿಸಿ ಮತ್ತು ಡಬ್ಬಿ ಅಥವಾ ಪ್ಯಾಕೆಟ್ ಉಳಿಸಿಕೊಳ್ಳಿ.",
                    "Call 108 or Poison Control immediately." to "ತಕ್ಷಣ 108 ಅಥವಾ ವಿಷ ನಿಯಂತ್ರಣಕ್ಕೆ ಕರೆ ಮಾಡಿ.",
                    "If unconscious, place the person in recovery position." to "ಪ್ರಜ್ಞೆ ಇಲ್ಲದಿದ್ದರೆ ವ್ಯಕ್ತಿಯನ್ನು ಚೇತರಿಕೆ ಸ್ಥಿತಿಯಲ್ಲಿ ಮಲಗಿಸಿ.",
                    "If lips or mouth are burned from chemical ingestion, rinse mouth with water." to "ರಾಸಾಯನಿಕ ಸೇವನೆಯಿಂದ ತುಟಿ ಅಥವಾ ಬಾಯಿ ಸುಟ್ಟಿದ್ದರೆ ಬಾಯಿಯನ್ನು ನೀರಿನಿಂದ ತೊಳೆಯಿರಿ.",
                    "Follow dispatcher instructions precisely." to "ಡಿಸ್ಪ್ಯಾಚರ್ ಸೂಚನೆಗಳನ್ನು ನಿಖರವಾಗಿ ಅನುಸರಿಸಿ."
                ),
                donts = listOf(
                    "Don't induce vomiting; it can worsen damage with corrosives or petroleum products." to "ವಾಂತಿ ಮಾಡಿಸಲು ಪ್ರಯತ್ನಿಸಬೇಡಿ; ಕರಗಿಸುವ ರಾಸಾಯನಿಕ ಅಥವಾ ಪೆಟ್ರೋಲಿಯಂ ಉತ್ಪನ್ನಗಳಲ್ಲಿ ಹಾನಿ ಹೆಚ್ಚಬಹುದು.",
                    "Don't give milk, water, or antidotes without medical guidance." to "ವೈದ್ಯಕೀಯ ಮಾರ್ಗದರ್ಶನವಿಲ್ಲದೆ ಹಾಲು, ನೀರು ಅಥವಾ ಪ್ರತಿವಿಷ ಕೊಡಬೇಡಿ.",
                    "Don't leave the person alone." to "ವ್ಯಕ್ತಿಯನ್ನು ಒಬ್ಬರೇ ಬಿಡಬೇಡಿ.",
                    "Don't wait for symptoms to worsen before calling for help." to "ಲಕ್ಷಣಗಳು ಹೆಚ್ಚಾಗುವವರೆಗೆ ಕಾಯದೆ ಸಹಾಯಕ್ಕೆ ಕರೆ ಮಾಡಿ."
                )
            )
        )

    private val fallbackHospitals: List<Hospital>
        get() = hospitals

    private val hospitals = listOf(
        Hospital("KC General Hospital", "Malleshwaram, Bengaluru", "08023341771"),
        Hospital("Victoria Hospital", "K.R. Market, Bengaluru", "08026701150"),
        Hospital("Jayanagar General Hospital", "Jayanagar, Bengaluru", "08026534600")
    )

    private data class Guide(
        val id: String,
        val titleEn: String,
        val titleKn: String,
        val icon: String,
        val color: Int,
        val dos: List<LocalText>,
        val donts: List<LocalText>
    ) {
        fun title(language: AppLanguage): String = if (language == AppLanguage.ENGLISH) titleEn else titleKn
        fun speech(language: AppLanguage): String {
            val doText = dos.joinToString(". ") { it.value(language) }
            val dontText = donts.joinToString(". ") { it.value(language) }
            return "${title(language)}. ${if (language == AppLanguage.ENGLISH) "Dos" else "ಮಾಡಿ"}. $doText. ${if (language == AppLanguage.ENGLISH) "Don'ts" else "ಮಾಡಬೇಡಿ"}. $dontText"
        }
    }

    private data class LocalText(val en: String, val kn: String) {
        fun value(language: AppLanguage): String = if (language == AppLanguage.ENGLISH) en else kn
    }

    private data class Hospital(val name: String, val area: String, val phone: String)

    private data class ProfileField(val key: String, val label: String, val inputType: Int)

    private enum class AppLanguage { KANNADA, ENGLISH }

    private sealed class Screen {
        object Language : Screen()
        object Login : Screen()
        object Home : Screen()
        data class Guide(val id: String) : Screen()
        object Hospitals : Screen()
        object Profile : Screen()
    }

    companion object {
        private const val LOCATION_REQUEST_CODE = 1081
    }
}
