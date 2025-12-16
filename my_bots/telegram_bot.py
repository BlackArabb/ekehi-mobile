import asyncio
import logging
from telegram import Update
from telegram.ext import Application, CommandHandler, ContextTypes

# Enable logging
logging.basicConfig(
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s", level=logging.INFO
)
logger = logging.getLogger(__name__)

# Replace with your actual bot token
BOT_TOKEN = "7969520183:AAGFM4gNVTqnPC_byBfclj--EBw7ytmL_JE"

async def start_command(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Send welcome message with user's Telegram ID"""
    user = update.effective_user
    user_id = user.id
    user_name = user.username or user.first_name or "User"
    
    welcome_message = (
        f"👋 Hello @{user_name}!\n\n"
        f"🎉 Welcome to Ekehi Task Bot!\n\n"
        f"🆔 Your Telegram User ID is: <code>{user_id}</code>\n\n"
        f"📋 How to use this ID:\n"
        f"1. Copy the ID above\n"
        f"2. Go back to the Ekehi app\n"
        f"3. Paste it in the Telegram verification screen\n"
        f"4. Submit to verify your membership\n\n"
        f"💡 Tip: Long press on the ID to copy it easily!"
    )
    
    await update.message.reply_text(welcome_message, parse_mode="HTML")

async def id_command(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Send user's Telegram ID again"""
    user = update.effective_user
    user_id = user.id
    user_name = user.username or user.first_name or "User"
    
    id_message = (
        f"👤 @{user_name}\n\n"
        f"🆔 Your Telegram User ID is: <code>{user_id}</code>\n\n"
        f"📋 Copy this ID and paste it in the Ekehi app to verify your Telegram membership."
    )
    
    await update.message.reply_text(id_message, parse_mode="HTML")

async def help_command(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Send help message"""
    help_message = (
        "🤖 <b>Ekehi Task Bot Help</b>\n\n"
        "This bot helps you verify your Telegram membership in the Ekehi app.\n\n"
        "<b>Commands:</b>\n"
        "/start - Get your Telegram ID (sent automatically)\n"
        "/id - Get your Telegram ID again\n"
        "/help - Show this help message\n\n"
        "<b>How to verify Telegram membership:</b>\n"
        "1. Open the Ekehi app\n"
        "2. Go to Social Tasks\n"
        "3. Find a Telegram task\n"
        "4. Click 'Complete'\n"
        "5. Come back to this bot and send /id\n"
        "6. Copy your ID and paste it in the Ekehi app\n"
        "7. Submit to verify"
    )
    
    await update.message.reply_text(help_message, parse_mode="HTML")

def main():
    """Start the bot"""
    # Create the Application and pass it your bot's token
    application = Application.builder().token(BOT_TOKEN).build()
    
    # Register command handlers
    application.add_handler(CommandHandler("start", start_command))
    application.add_handler(CommandHandler("id", id_command))
    application.add_handler(CommandHandler("help", help_command))
    
    # Run the bot until the user presses Ctrl-C
    print("🤖 Ekehi Task Bot is running...")
    print("Press Ctrl+C to stop the bot")
    application.run_polling(allowed_updates=Update.ALL_TYPES)

if __name__ == "__main__":
    main()