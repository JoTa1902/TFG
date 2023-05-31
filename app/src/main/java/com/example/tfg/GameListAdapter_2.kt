package com.example.tfg

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.Utils.launchGBGame
import com.example.tfg.Utils.launchNDSGame
import java.io.File

class GameListAdapter_2(private val context: Context, private var gameList: List<Games>) :
    RecyclerView.Adapter<GameListAdapter_2.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.game_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val juego = gameList[position]
        holder.bind(juego)

        holder.itemView.setOnClickListener {
            val downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val gameFilePath = File(downloadDirectory, juego.name)

            if (juego.platform == "NDS") {
               launchNDSGame(context, gameFilePath)
            } else if (juego.platform == "GBA") {
                launchGBGame(context, gameFilePath)
            }
        }
    }

    override fun getItemCount(): Int {
        return gameList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateGameList(newGameList: List<Games>) {
        gameList = newGameList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameImageView: ImageView = itemView.findViewById(R.id.gameImage)
        private val titleTextView: TextView = itemView.findViewById(R.id.gameName)
        private val genreTextView: TextView = itemView.findViewById(R.id.gameGenre)

        @SuppressLint("DiscouragedApi")
        fun bind(game: Games) {
            val imageUrl = game.image ?: ""
            val resourceId = context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
            gameImageView.setImageResource(resourceId)
            titleTextView.text = game.name
            genreTextView.text = game.genre
        }
    }
}
