# HiveNote: A Java Discord and Gemini Wrapper to Store Notes
>  ### HiveNote serves to store organized notes. People will be able to upload and retrieve their notes to and from a single database. Google Gemini will be used to summarize notes. All outputs will be formatted through LaTeX.
## Prerequisites:
1. A working Oracle Cloud Infrastructure ATP Database. Here is a tutorial on how to create one: https://amysimpsongrange.com/2023/01/23/creating-an-atp-database-in-oracle-cloud/
2. A Google Gemini API Token. They provide Always Free API Keys. https://ai.google.dev/
3. A Discord Application API Token. This allows you to create a Discord Bot. https://discord.com/developers/applications
## Instructions:
1. Set the environment variables of your API keys. The names can be found in the following classes:
  - HiveNoteBot.java
  - database/DatabaseServiceHandler.java
  - discord/OnReadyListener.java
2. Logs can be viewed in the logging/test.log file for possible errors
3. Have fun!

Note: The Google API Key must be an environment variable called `GOOGLE_API_KEY`
### Massive Note: Continous testing is still required to ensure a bug-free application.

## Future Add-ons:
1. Pagination System.
2. Summaries for multiple files instead of the last entry.
