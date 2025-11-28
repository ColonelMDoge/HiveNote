package discord;

import gemini.AISummaryService;
import latex.LatexConverter;
import net.dv8tion.jda.api.EmbedBuilder;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MessageListenerService extends ListenerAdapter {
    private final EmbedBuilder embedBuilder = new BotDescription();
    protected final String CHANNEL_NAME = "bot-channel";
    protected final int DISCORD_MESSAGE_CHAR_LIMIT = 2000;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);
        MessageChannel messageChannel = event.getGuildChannel();

        if (!event.isFromGuild()) return;
        if (event.getAuthor().isBot()) return;
        if (!messageChannel.getName().equals(CHANNEL_NAME)) return;

        String messageLine = event.getMessage().getContentRaw();

        if (messageLine.equalsIgnoreCase("!help hivenote")) {
            messageChannel.sendMessage("").setEmbeds(embedBuilder.build()).queue();
        }

        if (messageLine.startsWith("/ask ")) {
            String formattedMessage = messageLine.substring("/ask ".length());
            String returnedMessage = AISummaryService.generateResponse(formattedMessage);
            sendMessages(messageChannel, LatexConverter.extractLatexFromString(returnedMessage));
        }
    }

    // Determines whether the Object is a File or a String
    // Bypasses Discord's DISCORD_MESSAGE_CHAR_LIMIT by sending a message in parts
    private void sendMessages(MessageChannel channel, ArrayList<Object> list) {
        for (Object object : list) {
            if (object instanceof File) {
                try (FileUpload file = FileUpload.fromData((File) object)) {
                    channel.sendFiles(file).queue();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (object instanceof String) {
                if (((String) object).length() > DISCORD_MESSAGE_CHAR_LIMIT) {
                    do {
                        String cutMessage = object.toString().substring(0,DISCORD_MESSAGE_CHAR_LIMIT);
                        channel.sendMessage(cutMessage).queue();
                        object = object.toString().substring(cutMessage.length());
                    } while (((String) object).length() > DISCORD_MESSAGE_CHAR_LIMIT);
                }
                channel.sendMessage(object.toString()).queue();
            } else {
                System.out.println("Attempted to send a non String or File object!");
            }
        }
    }
}
