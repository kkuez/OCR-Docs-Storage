package com.Telegram.Processes;

import com.Telegram.Bot;
import com.Utils.BotUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ShoppingListProcess extends Process{

    public ShoppingListProcess(Bot bot, Update update){
        this.setBot(bot);
        String input = update.getMessage().getText();
        String cmd = input.substring(0, update.getMessage().getText().indexOf(" "));
        String arg = input.substring(input.indexOf(" ") + 1);
        switch (cmd){
            case "add":
                bot.getShoppingList().add(arg);
                BotUtil.sendMsg(update.getMessage().getChatId() + "", arg + " hinzugefügt! :)", bot);
                break;
            case "removeItem":
                try{
                    bot.getShoppingList().remove(Integer.parseInt(arg));
                    BotUtil.sendMsg(update.getMessage().getChatId() + "", arg + " gelöscht.", bot);
                }catch (Exception e){
                    e.printStackTrace();
                    BotUtil.sendMsg(update.getMessage().getChatId() + "", arg + " nicht gelöscht. Hast du eine Zahl aus der Liste angegeben? (/getList)", bot);
                }


                break;
            case "getList":
                StringBuilder listeBuilder = new StringBuilder();
                for(int i = 0;i<bot.getShoppingList().size();i++){
                    listeBuilder.append( i + ": " + bot.getShoppingList().get(i) + "\n");
                }
                BotUtil.sendMsg(update.getMessage().getChatId() + "", listeBuilder.toString(), bot);
                break;
        }
        bot.process = null;
    }
    @Override
    public void performNextStep(String arg, Update update) {

    }
}
