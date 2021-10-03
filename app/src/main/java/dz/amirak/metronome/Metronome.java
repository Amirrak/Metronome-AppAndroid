package dz.amirak.metronome;

import android.provider.MediaStore;
import android.util.Log;

public class Metronome {

    private int bpm;
    private int rythm;
    //private int noteValue;
    private int silenceSample;

    private double freqTick;
    private double freqTock;
    private final int beatSample = 1000;
    int sampleRate;

    private boolean play = true;

    private AudioMetronome audioMetronome;

    public Metronome(int bpm, int rythm, int sampleRate, double freqTick, double freqTock){
        this.bpm = bpm;
        this.sampleRate = sampleRate;
        this.freqTick = freqTick;
        this.freqTock = freqTock;
        this.rythm = rythm;
        audioMetronome = new AudioMetronome(sampleRate);
        audioMetronome.createPlayer();
    }

    public void calcSilence(){
        this.silenceSample = (int)(( (60.0/this.bpm)*this.sampleRate) - this.beatSample);
    }

    public void play(){
        calcSilence();
        double[] tick = audioMetronome.getSineWave(this.beatSample, this.sampleRate, this.freqTick);
        double[] tock = audioMetronome.getSineWave(this.beatSample, this.sampleRate, this.freqTock);
        double silence = 0;
        double[] sound = new double[this.sampleRate];
        int sampleCounter = 0;
        int silenceCounter = 0;
        int rythmCounter = 1;

        while(play){
            for(int i = 0; i < sound.length && play; i++){
                if(sampleCounter < this.beatSample){        // compteur est dans l interval de temps d'un son de battement
                    if(rythmCounter == 1)                    // first beat .. TICK
                        sound[i] = tick[sampleCounter];
                    else {                                    // other beat .. TOCK
                        sound[i] = tock[sampleCounter];
                    }
                    sampleCounter++;
                }else{
                    // compteur est dans l interval de temps de silence
                    sound[i] = silence;
                    silenceCounter++;
                    if (silenceCounter >= this.silenceSample){
                        sampleCounter = 0;
                        silenceCounter = 0;
                        rythmCounter++;
                        if(rythmCounter > (this.rythm)){
                            rythmCounter = 1;
                        }
                    }
                }
            }
            this.audioMetronome.writeSound(sound);
        }
    }

    public void stop() {
        this.play = false;
        this.audioMetronome.destroyAudioTrack();
    }
}
