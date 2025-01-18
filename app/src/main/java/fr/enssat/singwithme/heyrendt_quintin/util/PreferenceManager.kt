package fr.enssat.singwithme.heyrendt_quintin.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Classe utile pour stocker la playlist et les musiques pour une utilisation hors-ligne
 */
class PreferencesManager(context: Context) {
    // Initialise l'instance de SharedPreferences
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    /**
     * Sauvegarde une donnée dans le stockage
     *
     * @param key, la clé de sauvegarde
     * @param value, la valeur à enregistrer
     */
    fun saveData(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    /**
     * Récupère une donnée dans le stockage
     *
     * @param key, la clé de sauvegarde
     */
    fun getData(key: String): String? {
        return sharedPreferences.getString(key, "")
    }
}