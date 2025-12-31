import discord.CourseToTagLinker;
import discord.ModalListener;
import discord.OnReadyListener;
import discord.SlashCommandListener;
import logging.LoggerUtil;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.JDALogger;

public class HiveNoteBot {
    static {
        LoggerUtil.setupLogging();
    }
    private static final CourseToTagLinker courseToTagLinker = new CourseToTagLinker();
    private static final SlashCommandListener slashCommandListener = new SlashCommandListener(courseToTagLinker);
    private static final OnReadyListener onReadyListener = new OnReadyListener(courseToTagLinker, slashCommandListener);
    public static void main(String[] args) {
        final String TOKEN = System.getenv("JDA_TOKEN");
        System.setProperty("java.util.logging.manager", LoggerUtil.MyLogManager.class.getName());
        JDABuilder jdaBuilder = JDABuilder.createDefault(TOKEN);
        JDALogger.setFallbackLoggerEnabled(false);
        jdaBuilder.setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ONLINE_STATUS)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(onReadyListener, slashCommandListener, new ModalListener())
                .build();
    }
}