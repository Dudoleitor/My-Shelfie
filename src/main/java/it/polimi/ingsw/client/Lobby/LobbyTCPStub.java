package it.polimi.ingsw.client.Lobby;

import it.polimi.ingsw.shared.Move;
import it.polimi.ingsw.shared.Chat;
import it.polimi.ingsw.shared.RemoteInterfaces.LobbyRemoteInterface;

//TODO implement
public class LobbyTCPStub implements LobbyStub {
    @Override
    public void postToLiveChat(String playerName, String message) {

    }

    @Override
    public void postSecretToLiveChat(String sender, String receiver, String message) {

    }

    @Override
    public Chat updateLiveChat() {
        return null;
    }

    @Override
    public void quitGame(String player, LobbyRemoteInterface stub) {

    }

    @Override
    public boolean matchHasStarted() {
        return false;
    }

    @Override
    public void postMove(String player, Move move) {

    }

    @Override
    public boolean startGame(String player) {
        return false;
    }

    @Override
    public boolean isLobbyAdmin(String player) {
        return false;
    }

    @Override
    public int getID() {
        return 0;
    }
}
