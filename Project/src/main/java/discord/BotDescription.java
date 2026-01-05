package discord;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class BotDescription extends EmbedBuilder {

    // Separate class to store the general embed of the bot
    // This is so that this will not take up space in SlashCommandListener
    public BotDescription() {
        this.setTitle("HiveNote Discord Bot");
        this.setColor(new Color(235, 171, 0));
        this.setDescription("""
                HiveNote is a Discord bot that serves to immortalize course notes;\s
                whether it be paper or digital.\s
                All notes are combined together into a hive, grouped by course code and tags.\s
                Along with this, Google's Geminin AI will help to summarize\s
                everything into LaTeX documents, provide insight, or generate sample questions.

                Here are the list of the commands this bot can do:""");
        this.addField("Commands:", """
                ``/help`` - Opens this message again.
                ``/ask`` - Ask a question to the bot.
                ``/upload_note`` - Upload your notes to a respective database with given tags.
                ``/retrieve_note_by_id`` - Retrieves a note by its database ID.
                ``/retrieve_ids_from_filter`` - Retrieves note IDs from given filters.
                ``/generate_summary_by_id`` - Provides a brief summary of the note.
                ``/generateInsight`` - Provides insight and extra thoughts of the note.
                ``/generateQuestions`` - Provides sample questions for the note.
                ``/create_tag`` - Creates a tag for the database.
                ``/delete_tag`` - Deletes a tag from the database.
                ``/delete_note_by_id`` - Delete a note you own from the database.
                """, false);
    }
}
