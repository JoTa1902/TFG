import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.Games
import com.example.tfg.R
import com.example.tfg.Utils


class GameListAdapter(private val context: Context, private var gameList: List<Games>) : RecyclerView.Adapter<GameListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.game_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val juego = gameList[position]
        holder.bind(juego)
    }

    override fun getItemCount(): Int {
        return gameList.size
    }

    fun updateGameList(newGameList: List<Games>) {
        gameList = newGameList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val gameImageView: ImageView = itemView.findViewById(R.id.gameImage)
        private val titleTextView: TextView = itemView.findViewById(R.id.gameName)
        private val sizeTextView: TextView = itemView.findViewById(R.id.gameSize)
        private val genreTextView: TextView = itemView.findViewById(R.id.gameGenre)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(juego: Games) {
            val imageName = juego.image
            val resourceId = context.resources.getIdentifier(imageName, "drawable", context.packageName)

            gameImageView.setImageResource(resourceId)
            titleTextView.text = juego.name
            sizeTextView.text = juego.size
            genreTextView.text = juego.genre

        }

        override fun onClick(view: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val juego = gameList[position]
                Utils.downloadGame(context, juego.url, juego.name)
            }
        }

    }
}
