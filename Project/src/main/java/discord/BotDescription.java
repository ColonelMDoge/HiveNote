package discord;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class BotDescription extends EmbedBuilder {

    // Separate class to store the general embed of the bot
    // This is so that this will not take up space in MessageListenerService
    public BotDescription() {
        this.setTitle("HiveNote Discord Bot");
        this.setColor(new Color(235, 171, 0));
        this.setDescription("""
                HiveNote is a Discord bot that serves to immortalize course notes;\s
                whether it be paper or digital.\s
                All notes are combined together into a hive structure grouped by classifiers.\s
                Along with this, the HiveMind AI will help to summarize\s
                everything into LaTeX documents, provide insight, or generate sample questions.

                Here are the list of the commands this bot can do:""");
        this.addField("Commands:", """
                ``/help`` - Opens this message again.
                ``/ask`` - Ask a question to the bot.
                ``/createDB`` - Creates a unique database for users to join.
                ``/addUserToDB`` - Invites a user to join your notes database.
                ``/uploadNotes`` - Upload your notes to a respective database with given tags.
                ``/retrieveNotes`` - Retrieves notes based on provided tags.
                ``/generateSummary`` - Provides a brief summary of the note.
                ``/generateInsight`` - Provides insight and extra thoughts of the note.
                ``/generateQuestions`` - Provides sample questions for the note.
                ``/createTag`` - Creates a tag for the database.
                ``/deleteNotes`` - Delete a note you own from a database.
                ``/removeUserFromDB`` - Removes a user from a database. Admin only.
                ``/deleteDB`` - Deletes an entire database. Admin only.``""", false);
    }
}
