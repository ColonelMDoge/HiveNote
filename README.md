# HiveNote: A Java Discord and Gemini Wrapper to Store Notes
>  ### HiveNote serves to store organized notes. People will be able to upload and retrieve their notes to and from a single database. Google Gemini will be used to summarize notes. All outputs will be formatted through LaTeX.
## Prerequisites:
1. A working Oracle Cloud Infrastructure ATP Database. Here is a tutorial on how to create one: https://amysimpsongrange.com/2023/01/23/creating-an-atp-database-in-oracle-cloud/
2. A Google Gemini API Token. They provide Always Free API Keys. https://ai.google.dev/
3. A Discord Application API Token. This allows you to create a Discord Bot. https://discord.com/developers/applications
4. KaTeX and NodeJS via `npm install`
## Instructions:
1. Set the environment variables of your API keys. The names can be found in the following classes:
  - HiveNoteBot.java
  - database/DatabaseServiceHandler.java
  - discord/OnReadyListener.java
2. Logs can be viewed in the logging/logs.log file for possible errors
3. Have fun!
## Features:
1. LaTeX rendering using NodeJS via a Java ProcessBuilder
2. Pagination to store multiple files for an individual note ID
3. Gemini prompting for general questions and to summarize notes
4. If setup properly, this application can be hosted to run 24/7 without paying a single dollar

Note: The Google API Key must be an environment variable called `GOOGLE_API_KEY`
### Massive Note: Continous testing is still required to ensure a bug-free application.

## Future Add-ons:
1. Database indices for faster access.
2. Potential website integration to access notes online instead of from Discord.
