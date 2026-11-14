package com.example.data.ai

import android.graphics.Bitmap
import com.example.model.CameraShotAnglePreset
import com.example.model.VideoProject
import com.example.model.VideoScene
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import org.json.JSONObject

data class GeneratedStoryboard(
    val title: String,
    val synopsis: String,
    val globalLightingPreset: String,
    val scenes: List<VideoScene>
)

class AiCinemaService {

    private val storyboardSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Arabic title of the long-form video project"),
            "synopsis" to Schema.string(description = "Arabic summary of the full story and visual arc"),
            "globalLightingPreset" to Schema.enumeration(
                values = listOf("CINEMATIC_GOLD", "TEAL_ORANGE", "NOIR_CONTRAST", "DESERT_WARM", "CYBER_NEON", "NATURAL_8K"),
                description = "Recommended global lighting preset"
            ),
            "scenes" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "title" to Schema.string(description = "Arabic scene heading"),
                        "narrationAr" to Schema.string(description = "Rich, expressive Arabic voiceover narration script for this scene"),
                        "visualPrompt" to Schema.string(description = "Detailed photorealistic 8K cinema prompt describing environment, camera lens, PBR materials, and lighting"),
                        "durationSeconds" to Schema.integer(description = "Duration of this scene in seconds between 6 and 25"),
                        "cameraMovement" to Schema.enumeration(
                            values = listOf("STATIC", "PAN_LEFT", "PAN_RIGHT", "ZOOM_IN", "ZOOM_OUT", "DOLLY_FORWARD", "ORBIT_3D"),
                            description = "Cinematic camera movement"
                        ),
                        "cameraShotAngle" to Schema.enumeration(
                            values = listOf("EYE_LEVEL", "LOW_ANGLE", "HIGH_ANGLE", "AERIAL_DRONE", "DUTCH_ANGLE", "CLOSE_UP_MACRO"),
                            description = "Cinematic camera shot angle"
                        ),
                        "cameraPitchDeg" to Schema.integer(description = "Camera vertical tilt/pitch angle in degrees (-45 to 45)"),
                        "lensFocalMm" to Schema.integer(description = "Cinema lens focal length in mm (16, 24, 35, 50, or 85)"),
                        "transitionType" to Schema.enumeration(
                            values = listOf("CROSSFADE", "CUT", "DIP_TO_BLACK", "LIGHT_FLASH", "SLIDE_RTL"),
                            description = "Transition to next scene"
                        ),
                        "lightingKeyIntensity" to Schema.integer(description = "Key light intensity 0-100"),
                        "lightingFillIntensity" to Schema.integer(description = "Fill light intensity 0-100"),
                        "lightingRimIntensity" to Schema.integer(description = "Rim/Back light intensity 0-100"),
                        "colorTempKelvin" to Schema.integer(description = "Color temperature in Kelvin 2000-10000"),
                        "lightAngleDeg" to Schema.integer(description = "Key light azimuth angle 0-360"),
                        "lightElevationDeg" to Schema.integer(description = "Key light elevation angle 0-90"),
                        "atmosphereFog" to Schema.integer(description = "Volumetric fog and atmospheric haze 0-100"),
                        "colorGradePreset" to Schema.enumeration(
                            values = listOf("CINEMATIC_GOLD", "TEAL_ORANGE", "NOIR_CONTRAST", "DESERT_WARM", "CYBER_NEON", "NATURAL_8K"),
                            description = "Scene color grading preset"
                        )
                    )
                ),
                description = "Sequential list of 4 to 8 detailed scenes forming the long-form video"
            )
        )
    )

    private val sceneEnhanceSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Refined Arabic scene title"),
            "narrationAr" to Schema.string(description = "Refined expressive Arabic voiceover narration"),
            "visualPrompt" to Schema.string(description = "Enhanced photorealistic 8K visual prompt incorporating lighting and camera specs"),
            "cameraShotAngle" to Schema.enumeration(
                values = listOf("EYE_LEVEL", "LOW_ANGLE", "HIGH_ANGLE", "AERIAL_DRONE", "DUTCH_ANGLE", "CLOSE_UP_MACRO"),
                description = "Recommended camera shot angle"
            ),
            "cameraPitchDeg" to Schema.integer(description = "Recommended camera pitch angle -45 to 45"),
            "lensFocalMm" to Schema.integer(description = "Recommended lens focal length (16, 24, 35, 50, 85)"),
            "lightingKeyIntensity" to Schema.integer(description = "Recommended key light 0-100"),
            "lightingFillIntensity" to Schema.integer(description = "Recommended fill light 0-100"),
            "lightingRimIntensity" to Schema.integer(description = "Recommended rim light 0-100"),
            "colorTempKelvin" to Schema.integer(description = "Recommended color temperature 2000-10000"),
            "lightAngleDeg" to Schema.integer(description = "Recommended light angle 0-360"),
            "lightElevationDeg" to Schema.integer(description = "Recommended light elevation 0-90"),
            "atmosphereFog" to Schema.integer(description = "Recommended volumetric fog 0-100"),
            "colorGradePreset" to Schema.enumeration(
                values = listOf("CINEMATIC_GOLD", "TEAL_ORANGE", "NOIR_CONTRAST", "DESERT_WARM", "CYBER_NEON", "NATURAL_8K"),
                description = "Recommended color grade preset"
            )
        )
    )

    private val storyboardModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = storyboardSchema
            }
        )
    }

    private val sceneEnhanceModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = sceneEnhanceSchema
            }
        )
    }

    private val textSequenceModel by lazy {
        Firebase.ai.generativeModel(modelName = "gemini-3.8-flash")
    }

    private val imageGenModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-nano-banana-2.1",
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.IMAGE)
            }
        )
    }

    suspend fun generateLongVideoStoryboard(
        ideaPrompt: String,
        genre: String,
        visualStyle: String,
        requestedSceneCount: Int,
        referenceBitmap: Bitmap? = null
    ): GeneratedStoryboard {
        val systemInstruction = """
            أنت مخرج سينمائي محترف ومهندس إضاءة وتصوير واقعي (8K Photorealistic Director).
            قم بتحويل فكرة المستخدم إلى مخطط فيلم طويل متكامل يتكون من $requestedSceneCount مشاهد مترابطة.
            النوع السينمائي: $genre.
            الأسلوب البصري: $visualStyle.
            التزم بأن يكون السرد الصوتي (narrationAr) باللغة العربية الفصحى السلسة والمؤثرة سينمائياً،
            وأن يكون الوصف البصري (visualPrompt) دقيقاً جداً (يتضمن زاوية الكاميرا، البعد البؤري للعدسة مثل 35mm Anamorphic، الإضاءة الفيزيائية PBR، Global Illumination، وتفاصيل البيئة الواقعية).
        """.trimIndent()

        val response = if (referenceBitmap != null) {
            storyboardModel.generateContent(
                content {
                    image(referenceBitmap)
                    text("$systemInstruction\n\nاستلهم البيئة والشخصيات والإضاءة من هذه الصورة المرجعية لبناء مشاهد الفيديو الطويل حول الفكرة التالية:\n$ideaPrompt")
                }
            )
        } else {
            storyboardModel.generateContent("$systemInstruction\n\nفكرة الفيديو المطلوب:\n$ideaPrompt")
        }

        val jsonText = response.text ?: throw IllegalStateException("لم يرجع النموذج استجابة نصية.")
        return parseStoryboardJson(jsonText)
    }

    suspend fun enhanceSceneAndLighting(
        scene: VideoScene,
        directorInstructionAr: String
    ): VideoScene {
        val prompt = """
            أنت مخرج إضاءة وتصوير سينمائي (Director of Photography).
            لدينا المشهد التالي:
            العنوان: ${scene.title}
            السرد العربي: ${scene.narrationAr}
            الوصف البصري الحالي: ${scene.visualPrompt}
            زاوية الكاميرا الحالية: ${scene.cameraShotAngle} (Pitch=${scene.cameraPitchDeg}°, Lens=${scene.lensFocalMm}mm)
            إعدادات الإضاءة الحالية: Key=${scene.lightingKeyIntensity}%, Fill=${scene.lightingFillIntensity}%, Rim=${scene.lightingRimIntensity}%, Kelvin=${scene.colorTempKelvin}K, Fog=${scene.atmosphereFog}%
            
            مطلوب تعديل وتطوير هذا المشهد وزاوية الكاميرا وإضاءته بناءً على توجيه المخرج التالي:
            "$directorInstructionAr"
        """.trimIndent()

        val response = sceneEnhanceModel.generateContent(prompt)
        val jsonText = response.text ?: throw IllegalStateException("تعذر تحديث المشهد بالذكاء الاصطناعي.")
        val obj = JSONObject(jsonText)

        return scene.copy(
            title = obj.optString("title", scene.title),
            narrationAr = obj.optString("narrationAr", scene.narrationAr),
            visualPrompt = obj.optString("visualPrompt", scene.visualPrompt),
            cameraShotAngle = obj.optString("cameraShotAngle", scene.cameraShotAngle),
            cameraPitchDeg = obj.optLong("cameraPitchDeg", scene.cameraPitchDeg).coerceIn(-45L, 45L),
            lensFocalMm = normalizeLensMm(obj.optLong("lensFocalMm", scene.lensFocalMm)),
            lightingKeyIntensity = obj.optLong("lightingKeyIntensity", scene.lightingKeyIntensity).coerceIn(0L, 100L),
            lightingFillIntensity = obj.optLong("lightingFillIntensity", scene.lightingFillIntensity).coerceIn(0L, 100L),
            lightingRimIntensity = obj.optLong("lightingRimIntensity", scene.lightingRimIntensity).coerceIn(0L, 100L),
            colorTempKelvin = obj.optLong("colorTempKelvin", scene.colorTempKelvin).coerceIn(2000L, 10000L),
            lightAngleDeg = obj.optLong("lightAngleDeg", scene.lightAngleDeg).coerceIn(0L, 360L),
            lightElevationDeg = obj.optLong("lightElevationDeg", scene.lightElevationDeg).coerceIn(0L, 90L),
            atmosphereFog = obj.optLong("atmosphereFog", scene.atmosphereFog).coerceIn(0L, 100L),
            colorGradePreset = obj.optString("colorGradePreset", scene.colorGradePreset)
        )
    }

    suspend fun generateVideoPromptSequenceDocument(
        project: VideoProject,
        scenes: List<VideoScene>
    ): String {
        val scenesSummary = scenes.mapIndexed { idx, s ->
            "مشهد ${idx + 1} (${s.title} - ${s.durationSeconds}ث): حركة=${s.cameraMovement}, زاوية=${s.cameraShotAngle}, عدسة=${s.lensFocalMm}mm, إضاءة=${s.colorTempKelvin}K Key=${s.lightingKeyIntensity}% | الوصف: ${s.visualPrompt}"
        }.joinToString("\n")

        val prompt = """
            أنت مهندس موجهات فيديو سينمائية (AI Video Prompt Sequence Engineer).
            قم بصياغة دليل تسلسل موجهات الفيديو (Video Prompt Sequence) باللغتين العربية والإنجليزية للمشروع التالي:
            عنوان الفيلم: ${project.title}
            الأبعاد: ${project.aspectRatio} | الأسلوب: ${project.visualStyle}
            المشاهد الحالية:
            $scenesSummary
            
            اكتب لكل مشهد:
            1. وصف اللقطة وزاوية الكاميرا والإضاءة بالعربية.
            2. موجه التوليد الفيزيائي الدقيق (Master Video Prompt) جاهز للنسخ والاستخدام.
        """.trimIndent()

        val response = textSequenceModel.generateContent(prompt)
        return response.text ?: throw IllegalStateException("لم يرجع النموذج تسلسل الموجهات.")
    }

    suspend fun generatePhotorealisticSceneFrame(
        scene: VideoScene,
        referenceBitmap: Bitmap? = null
    ): Bitmap? {
        val shotAngle = CameraShotAnglePreset.fromCode(scene.cameraShotAngle)
        val lightingSpec = buildString {
            append("Photorealistic 8K cinematic film still, ARRI Alexa 65 ${scene.lensFocalMm}mm anamorphic lens, ")
            append("${shotAngle.promptTag}, camera vertical pitch ${scene.cameraPitchDeg} degrees, ")
            append("PBR materials, ray-traced reflections. ")
            append("Scene description: ${scene.visualPrompt}. ")
            append("Studio 3-Point Lighting setup: ")
            append("Key light intensity ${scene.lightingKeyIntensity}% at azimuth angle ${scene.lightAngleDeg} degrees and elevation ${scene.lightElevationDeg} degrees, ")
            append("Fill light ${scene.lightingFillIntensity}%, ")
            append("Rim backlight ${scene.lightingRimIntensity}%, ")
            append("Color temperature ${scene.colorTempKelvin}K, ")
            append("Volumetric atmospheric fog ${scene.atmosphereFog}%, ")
            append("Color grading preset: ${scene.colorGradePreset}. No text or watermarks.")
        }

        val response = if (referenceBitmap != null) {
            imageGenModel.generateContent(
                content {
                    image(referenceBitmap)
                    text("Transform and relight this reference image into a photorealistic cinematic frame matching: $lightingSpec")
                }
            )
        } else {
            imageGenModel.generateContent(lightingSpec)
        }

        val imagePart = response.candidates.firstOrNull()
            ?.content?.parts?.filterIsInstance<ImagePart>()
            ?.firstOrNull()

        return imagePart?.image
    }

    private fun parseStoryboardJson(jsonString: String): GeneratedStoryboard {
        val root = JSONObject(jsonString)
        val title = root.optString("title", "فيلم وثائقي سينمائي")
        val synopsis = root.optString("synopsis", "")
        val globalPreset = root.optString("globalLightingPreset", "CINEMATIC_GOLD")
        val scenesArray = root.optJSONArray("scenes")
        val parsedScenes = mutableListOf<VideoScene>()

        if (scenesArray != null) {
            for (i in 0 until scenesArray.length()) {
                val item = scenesArray.getJSONObject(i)
                val shotAngle = item.optString("cameraShotAngle", "EYE_LEVEL")
                val pitch = item.optLong("cameraPitchDeg", 0L).coerceIn(-45L, 45L)
                val lens = normalizeLensMm(item.optLong("lensFocalMm", 35L))
                parsedScenes.add(
                    VideoScene(
                        sceneIndex = i.toLong(),
                        title = item.optString("title", "المشهد ${i + 1}"),
                        narrationAr = item.optString("narrationAr", ""),
                        visualPrompt = item.optString("visualPrompt", "Photorealistic 8K cinematic shot"),
                        durationSeconds = item.optLong("durationSeconds", 10L).coerceIn(3L, 60L),
                        cameraMovement = item.optString("cameraMovement", "DOLLY_FORWARD"),
                        transitionType = item.optString("transitionType", "CROSSFADE"),
                        lightingKeyIntensity = item.optLong("lightingKeyIntensity", 85L).coerceIn(0L, 100L),
                        lightingFillIntensity = item.optLong("lightingFillIntensity", 45L).coerceIn(0L, 100L),
                        lightingRimIntensity = item.optLong("lightingRimIntensity", 70L).coerceIn(0L, 100L),
                        colorTempKelvin = item.optLong("colorTempKelvin", 4500L).coerceIn(2000L, 10000L),
                        lightAngleDeg = item.optLong("lightAngleDeg", 45L).coerceIn(0L, 360L),
                        lightElevationDeg = item.optLong("lightElevationDeg", 30L).coerceIn(0L, 90L),
                        atmosphereFog = item.optLong("atmosphereFog", 25L).coerceIn(0L, 100L),
                        colorGradePreset = item.optString("colorGradePreset", globalPreset),
                        cameraShotAngle = shotAngle,
                        cameraPitchDeg = pitch,
                        lensFocalMm = lens
                    )
                )
            }
        }

        return GeneratedStoryboard(
            title = title,
            synopsis = synopsis,
            globalLightingPreset = globalPreset,
            scenes = parsedScenes
        )
    }

    private fun normalizeLensMm(mm: Long): Long {
        val supported = listOf(16L, 24L, 35L, 50L, 85L)
        return supported.minByOrNull { kotlin.math.abs(it - mm) } ?: 35L
    }
}
