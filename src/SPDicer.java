package scripts.SPDicer.src;

import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.AdvancedMessageListener;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.api.wrappers.widgets.message.Message;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Created by Adar on 6/22/17.
 */

@ScriptManifest(category= Category.MONEYMAKING, author="youngsp", name="SPDicer", description="Hosts public chat dicing at whatever odds you would like.", version=1.0)
public class SPDicer extends AbstractScript implements AdvancedMessageListener {

    // Paint Variables
    private int startingGP = 0;
    private int gpGained = 0;
    private long timeBegan;
    private long timeRan;
    private String status;

    private Area startingArea;
    private boolean inABet = false;
    private boolean shouldAccept = false;
    private boolean shouldRoll = false;
    private boolean shouldPayout = false;
    private boolean tradeWaiting = false;
    private boolean payoutInTrade = false;
    private String playerToTrade;

    private int currentBet = 0;
    private int coinAmt = 0;
    private int currentGP = 0;
    private long timeTradeStarted;
    private boolean timingTrade = false;

    private GUI gui = null;
    ScriptVars sv = new ScriptVars();

    private final String COINS_NAME = "Coins";
    private final String ACCEPTED_TRADE_STR = "Other player has accepted.";
    private final int FIRST_TRADE_WIDGET_ID = 335;
    private final int FIRST_TRADE_CHILD_ID = 30;
    private final int SECOND_TRADE_WIDGET_ID = 334;
    private final int SECOND_TRADE_CHILD_ID = 4;


    @Override
    public void onPaint(Graphics g) {
        super.onPaint(g);

        timeRan = System.currentTimeMillis() - timeBegan;

        String scriptName = "SPDicer by YoungSP aka Spades";
        Color bgColor = Color.BLACK;
        Color textColor = Color.WHITE;
        int x = 8;
        int y = 471;

        FontMetrics fm = g.getFontMetrics();
        Rectangle2D rect = fm.getStringBounds(scriptName, g);

        g.setColor(bgColor);
        g.fillRect(
                x,
                y - fm.getAscent(),
                (int) rect.getWidth(),
                (int) rect.getHeight()
        );

        g.fillRect(7, 345, 500, 120);

        g.setColor(textColor);
        g.drawString("RUNNING FOR: " + Util.getRunTime(timeRan), 20, 290);
        g.drawString("GP Gained: " + Util.withSuffix(gpGained), 20, 322);
        g.drawString(scriptName, x, y);
    }

    @Override
    public void onStart() {
        getMouse().getMouseSettings().setWordsPerMinute(Calculations.random(105, 145));

        gui = new GUI(sv);
        gui.setVisible(true);

        int x = getLocalPlayer().getX();
        int y = getLocalPlayer().getY();
        int radius = 2;
        startingArea = Area.generateArea(radius, new Tile(x, y));

        Item coins = getInventory().get(COINS_NAME);
        if (coins != null)
            startingGP = coins.getAmount();

        timeBegan = System.currentTimeMillis();
    }

    @Override
    public int onLoop() {

        if (!startingArea.contains(getLocalPlayer())) {
            getWalking().walk(startingArea.getRandomTile());
            log("RUN TO STARTING AREA");
            return Calculations.random(1000, 2500);
        }

        State state = getState();
        switch(state) {
            case SPAMMING:
                status = "Spamming";
                handleSpamming();
                sleep(sv.spamTimerMS);
                break;
            case TRADING:
                status = "Trading";
                handleTrading();
                break;
            case ROLLING:
                status = "Rolling";
                handleRolling();
                break;
            case PAYING:
                status = "Paying";
                handlePaying();
                break;
            case GUI:
                status = "GUI";
                handleGUI();
                break;
        }

        return Calculations.random(150, 350);
    }

    private State getState() {
        if (!sv.started) {
            return State.GUI;
        }

        if (getTrade().isOpen() && !shouldPayout) {
            return State.TRADING;
        } else if (shouldRoll) {
            return State.ROLLING;
        } else if (shouldPayout) {
            return State.PAYING;
        } else return State.SPAMMING;

    }

    private enum State {
        GUI, SPAMMING, TRADING, ROLLING, PAYING
    }

    private void handleGUI() {
        if (gui == null) {
            gui = new GUI(sv);
            sleep(300);
        } else if (!gui.isVisible() && !sv.started) {
            gui.setVisible(true);
            sleep(1000);
        } else {
            if (!sv.started) {
                sleep(300);
            } else {
                sv.started = true;
            }
        }
    }

    private void handleSpamming() {
        timingTrade = false;
        timeTradeStarted = 0;
        if (tradeWaiting) {
            getTrade().tradeWithPlayer(playerToTrade);
            tradeWaiting = false;
            sleepUntil(() -> getTrade().isOpen() || !startingArea.contains(getLocalPlayer()), Calculations.random(3000, 7500));
        } else {
            String playerName = getLocalPlayer().getName();
            String prettyMin = Util.withSuffix(sv.minBet);
            String prettyMax = Util.withSuffix(sv.maxBet);
            getKeyboard().type(sv.effects + "Public Chat " + sv.odds + "*2 - " + playerName + " - Min: " + prettyMin + " Max: " + prettyMax);
        }
    }

    private void handleTrading() {
        if (!timingTrade) {
            log("Timing");
            timeTradeStarted = System.currentTimeMillis();
            timingTrade = true;
        }

        if (timingTrade && System.currentTimeMillis() - timeTradeStarted > 30000) {
            getTrade().declineTrade();
            getKeyboard().type(playerToTrade + ", You're taking too long. Trade me again.");
            timingTrade = false;
            timeTradeStarted = 0;
            return;
        }

        Item[] theirItems = getTrade().getTheirItems();
        if (theirItems != null) {
            for (int i=0; i < theirItems.length; i++) {
                Item itemI = theirItems[i];
                if (itemI.getName().equalsIgnoreCase(COINS_NAME)) {
                    coinAmt = itemI.getAmount();
                    if (coinAmt >= sv.minBet && coinAmt <= sv.maxBet && theirItems.length == 1) {
                        shouldAccept = true;
                    } else {
                        shouldAccept = false;
                    }
                }
            }
        } else shouldAccept = false;

        if (shouldAccept) {
            if (getTrade().isOpen(1)) {
                WidgetChild firstTradeChild =
                        getWidgets().getWidget(FIRST_TRADE_WIDGET_ID)
                                .getChild(FIRST_TRADE_CHILD_ID);

                String tradeText = firstTradeChild.getText();
                if (tradeText != null) {
                    if (tradeText.equalsIgnoreCase(ACCEPTED_TRADE_STR)) {
                        getTrade().acceptTrade(1);
                    }
                }
            } else if (getTrade().isOpen(2)) {

                boolean shouldDecline = true;

                theirItems = getTrade().getTheirItems();
                if (theirItems != null) {
                    for (int i=0; i < theirItems.length; i++) {
                        Item itemI = theirItems[i];
                        if (itemI.getName().equalsIgnoreCase(COINS_NAME)) {
                            coinAmt = itemI.getAmount();
                            if (coinAmt >= sv.minBet && coinAmt <= sv.maxBet && theirItems.length == 1) {
                                shouldDecline = false;
                            } else {
                                shouldDecline = true;
                            }
                        }
                    }
                }

                if (shouldDecline) {
                    getTrade().declineTrade();
                    getKeyboard().type("Min is " + Util.withSuffix(sv.minBet) + " Max is " + Util.withSuffix(sv.maxBet));
                    return;
                }

                WidgetChild firstTradeChild =
                        getWidgets().getWidget(SECOND_TRADE_WIDGET_ID)
                                .getChild(SECOND_TRADE_CHILD_ID);

                String tradeText = firstTradeChild.getText();

                if (tradeText != null) {
                    if (tradeText.equalsIgnoreCase(ACCEPTED_TRADE_STR)) {
                        currentGP = getInventory().get(COINS_NAME).getAmount();
                        playerToTrade = getTrade().getTradingWith();
                        if (getTrade().acceptTrade(2)) {
                            sleepUntil(() -> !getTrade().isOpen(), 20000);
                            currentBet = coinAmt;

                            if (getInventory().get(COINS_NAME).getAmount() == currentGP + currentBet) {
                                shouldRoll = true;
                                inABet = true;
                            } else {
                                shouldRoll = false;
                                inABet = false;
                                currentBet = 0;
                                getKeyboard().type("Lolol " + playerToTrade + " just tried to scam me haha");
                                playerToTrade = "";
                            }

                        }

                    }
                }
            }
        } else {
            if (getTrade().isOpen(1)) {
                WidgetChild firstTradeChild =
                        getWidgets().getWidget(FIRST_TRADE_WIDGET_ID)
                                .getChild(FIRST_TRADE_CHILD_ID);

                String tradeText = firstTradeChild.getText();
                if (tradeText != null) {
                    if (tradeText.equalsIgnoreCase(ACCEPTED_TRADE_STR)) {
                        getKeyboard().type("Min is " + Util.withSuffix(sv.minBet) + " Max is " + Util.withSuffix(sv.maxBet));
                        getTrade().declineTrade();
                    }
                }
            }
        }
    }

    private void handleRolling() {
        getKeyboard().type("purple: Took " + Util.withSuffix(currentBet) + " bet from " + playerToTrade);
        int roll = Calculations.random(1, 100);
        boolean shouldReroll = (roll == sv.odds);
        boolean didPlayerWin = (roll > sv.odds);

        String rollResult;
        String resultColor;

        log("CURRENT BET: " + currentBet);

        if (shouldReroll) {
            return;
        } else {
            if (didPlayerWin) {
                rollResult = "They won.";
                resultColor = "green:";
                shouldPayout = true;
                shouldRoll = false;
                gpGained -= currentBet;
            } else {
                rollResult = "They lost.";
                resultColor = "red:";
                shouldPayout = false;
                shouldRoll = false;
                shouldAccept = false;
                gpGained += currentBet;
                currentBet = 0;
            }
        }

        getKeyboard().type(
                resultColor
                        + " "
                        + playerToTrade
                        + "'s roll is "
                        + roll
                        + ". "
                        + rollResult
        );

        if (!didPlayerWin) {
            inABet = false;
            playerToTrade = "";
        }
    }

    private void handlePaying() {
        int payout = currentBet * 2;
        if (!getTrade().isOpen()) {
            payoutInTrade = false;
            if (getTrade().tradeWithPlayer(playerToTrade)) {
                sleepUntil(() -> getTrade().isOpen(), Calculations.random(3000, 10000));
            }
        } else {
            Item[] myItems = getTrade().getMyItems();
            if (myItems != null) {
                for (int i=0; i < myItems.length; i++) {
                    Item itemI = myItems[i];
                    if (itemI.getName() == COINS_NAME) {
                        int coinAmt = itemI.getAmount();
                        if (coinAmt == payout) {
                            payoutInTrade = true;
                        } else {
                            getTrade().removeItem(COINS_NAME, coinAmt);
                            payoutInTrade = false;
                        }
                    }
                }
            }

            if (getTrade().isOpen(1)) {
                if (payoutInTrade) {
                    getTrade().acceptTrade(1);
                } else {
                    if (getTrade().addItem(COINS_NAME, payout)) {
                        payoutInTrade = true;
                        return;
                    } else {
                        log("CANT ADD PAYOUT: " + payout);
                    }
                }
            } else if (getTrade().isOpen(2)) {
                if (getWidgets().getWidget(SECOND_TRADE_WIDGET_ID)
                        .getChild(SECOND_TRADE_CHILD_ID)
                        .getText().equalsIgnoreCase(ACCEPTED_TRADE_STR)) {
                    getTrade().acceptTrade(2);
                    getKeyboard().type("cyan: Paid Out " + Util.withSuffix(payout) + " to " + playerToTrade);
                    shouldRoll = false;
                    currentBet = 0;
                    payoutInTrade = false;
                    shouldPayout = false;
                    tradeWaiting = false;
                    shouldAccept = false;
                    coinAmt = 0;
                    inABet = false;
                    playerToTrade = "";
                    sleepUntil(() -> !getTrade().isOpen(), 60000);
                }
            }
        }
    }

    @Override
    public void onTradeMessage(Message message) {
        if (!tradeWaiting && !inABet) {
            playerToTrade = message.getUsername();
            tradeWaiting = true;
        }
        return;
    }

    @Override
    public void onClanMessage(Message message) {
        return;
    }

    @Override
    public GameState onGameState(GameState gameState) {
        return super.onGameState(gameState);
    }

    @Override
    public void onGameMessage(Message message) {
        return;
    }

    @Override
    public void onPrivateInfoMessage(Message message) {
        return;
    }

    @Override
    public void onPrivateOutMessage(Message message) {
        return;
    }

    @Override
    public void onPrivateInMessage(Message message) {
        return;
    }

    @Override
    public void onPlayerMessage(Message message) {
        return;
    }

    @Override
    public void onAutoMessage(Message message) {
        return;
    }
}
