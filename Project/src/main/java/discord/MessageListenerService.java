package discord;

import gemini.AISummaryService;
import net.dv8tion.jda.api.EmbedBuilder;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class MessageListenerService extends ListenerAdapter {
    final String CHANNEL_NAME = "bot-channel";

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);
        MessageChannel messageChannel = event.getGuildChannel();

        if (!event.isFromGuild()) return;
        if (event.getAuthor().isBot()) return;
        if (!messageChannel.getName().equals(CHANNEL_NAME)) return;

        String messageLine = event.getMessage().getContentRaw();
        EmbedBuilder helpEmbedMessage = new EmbedBuilder();
        helpEmbedMessage.setColor(new Color(235, 171, 0));

        if (messageLine.equalsIgnoreCase("!help hivenote")) {
            helpEmbedMessage.setTitle("HiveNote Discord Bot");
            helpEmbedMessage.setDescription("""
                HiveNote is a Discord bot that serves to immortalize course notes;\s
                whether it be paper or digital.\s
                All notes are combined together into a hive structure grouped by classifiers.\s
                Along with this, the HiveMind AI will help to summarize\s
                everything into LaTeX documents, provide insight, or generate sample questions.

                Here are the list of the commands this bot can do:""");
            helpEmbedMessage.addField("Commands:", """
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
            messageChannel.sendMessage("").setEmbeds(helpEmbedMessage.build()).queue();
        }

        if (messageLine.startsWith("/ask ")) {
            String formattedMessage = messageLine.substring("/ask ".length());
            String returnedMessage = AISummaryService.generateResponse(formattedMessage);
            messageChannel.sendMessage(returnedMessage).queue();
        }
    }
}
