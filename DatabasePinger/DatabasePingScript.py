# This file is used to continuously ping an Always-Free Oracle database to keep it running 24/7 without it shutting down

import oracledb
import schedule
import time
import os
from dotenv import load_dotenv

load_dotenv()

def ping_db():
    conn = oracledb.connect(
        user=os.getenv("DB_USER"),
        password=os.getenv("DB_PASSWORD"),
        dsn=os.getenv("DB_DSN")
    )
    cursor = conn.cursor()
    cursor.execute("SELECT 1 FROM DUAL")
    conn.close()
    print("DB pinged")

ping_db() # Initial check to see if database connection works

schedule.every(24).hours.do(ping_db)

while True:
    schedule.run_pending()
    time.sleep(1)