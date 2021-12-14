package com.tojoy.musicplayer.manager;

import java.util.Observable;

public class MusicSubjectObservable extends Observable {

    public MusicSubjectObservable(){

    }
    public void updataSubjectObserivce(Object data){
        setChanged();
        notifyObservers(data);
    }
}
