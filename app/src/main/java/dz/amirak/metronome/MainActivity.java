package dz.amirak.metronome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Runnable metronomeClick;
    private Thread t2 = new Thread();

    private GestureDetectorCompat mGestureDetectorTempo;
    private GestureDetectorCompat mGestureDetectorRythm;

    private boolean state = false;// click = true;

    private TextView tempoText, rythm1Text, TAPtext, tempoName, pourcentageText;
    private View tempoView, rythmView, root;


    private int tempo = 120;
    private int tempoMS = 500;
    private int tempoDelay = 0, rythm1Delay = 0, pourcentageDelay = 0;

    private int rythm1 = 4, pourcentage = 100, TAP = 0;

    private int sampleRate = 8000;
    private Metronome metronome = new Metronome(tempo, rythm1, sampleRate, 440, 220);

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        metronomeClick = new Runnable() {
            @Override
            public void run() {
                if (TAP >= rythm1)
                    TAP = 1;
                else
                    TAP += 1;
                TAPtext.setText("" + TAP);

                mHandler.postDelayed(this, tempoMS);
            }
        };


        setTheme(R.style.Theme_Metronome);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setNavigationBarColor(getResources().getColor(R.color.bg_stop));
        getWindow().setStatusBarColor(getResources().getColor(R.color.bg_stop));
        this.tempoText = findViewById(R.id.tempo);
        this.rythm1Text = findViewById(R.id.rythm1);
        this.TAPtext = findViewById(R.id.TAP);
        this.tempoName = findViewById(R.id.tempoName);
        this.pourcentageText = findViewById(R.id.pourcentage);

        this.tempoView = findViewById(R.id.tempoView);
        this.rythmView = findViewById(R.id.rythm1View);
        this.root = findViewById(R.id.rootView);


        tempoText.setText("" + tempo);

        mGestureDetectorTempo = new GestureDetectorCompat(this, new GestureListenerTempo());
        mGestureDetectorRythm = new GestureDetectorCompat(this, new GestureListenerRythm());
        mGestureDetectorTempo.setIsLongpressEnabled(false);
        mGestureDetectorRythm.setIsLongpressEnabled(false);

        tempoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetectorTempo.onTouchEvent(event);
                if(state) {
                    // if metronome is running, and release action is detectected
                    // (when scroll to change tempo, onDown pause the metronome first, and action_up replay it)
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        metronomeClick.run();
                        if(!t2.isAlive()) {
                            t2 = new Thread() {

                                @Override
                                public void run() {
                                    metronome.setBpm(tempo);
                                    metronome.setRythm(rythm1);
                                    metronome.play();
                                    return;
                                }

                                @Override
                                public void interrupt() {
                                    metronome.stop();
                                    metronome.setPlay(false);
                                    super.interrupt();
                                }
                            };
                            t2.start();
                        }
                    }
                }
                return true;
            }
        });

        rythmView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetectorRythm.onTouchEvent(event);
                if(state) {
                    // if metronome is running, and release action is detectected
                    // (when scroll to change tempo, onDown pause the metronome first, and action_up replay it)
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        metronomeClick.run();
                        t2 = new Thread(){

                            @Override
                            public void run() {
                                metronome = new Metronome(tempo, rythm1,sampleRate, 440,220);
                                metronome.play();
                                super.run();
                            }

                            @Override
                            public void interrupt() {
                                metronome.stop();
                                super.interrupt();
                            }
                        };
                        t2.start();
                    }
                }
                return true;
            }
        });
    }


    public void startStopCount () {
        if (state) {
            state = false;
            if(t2.isAlive()) {
                t2.interrupt();
            }
            root.setBackgroundColor(getResources().getColor(R.color.bg_stop));
            tempoText.setTextColor(getResources().getColor(R.color.textStop));
            tempoName.setTextColor(getResources().getColor(R.color.textStop));
            TAPtext.setVisibility(View.INVISIBLE);
            pourcentageText.setVisibility(View.GONE);
            rythm1Text.setVisibility(View.VISIBLE);
            mHandler.removeCallbacks(metronomeClick);
            getWindow().setNavigationBarColor(getResources().getColor(R.color.bg_stop));
            getWindow().setStatusBarColor(getResources().getColor(R.color.bg_stop));
            TAP = 0;
            TAPtext.setText("" + TAP);
        } else {
            state = true;
            root.setBackgroundColor(getResources().getColor(R.color.bg_start));
            tempoText.setTextColor(getResources().getColor(R.color.textStart));
            tempoName.setTextColor(getResources().getColor(R.color.textStart));
            TAPtext.setVisibility(View.VISIBLE);
            rythm1Text.setVisibility(View.GONE);
            pourcentageText.setVisibility(View.VISIBLE);
            getWindow().setNavigationBarColor(getResources().getColor(R.color.bg_start));
            getWindow().setStatusBarColor(getResources().getColor(R.color.bg_start));
        }
    }

    private class GestureListenerTempo extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            mHandler.removeCallbacks(metronomeClick);
            if(t2.isAlive()) {
                t2.interrupt();
            }
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // when metronome is paused, we use onSingleTapUp to start it. (and not the action_up, 'cause it will start the metronome
            // when choosing the tempo with the scroll), so onSingleTapUp set state=true, then action_up start the metronome
            startStopCount();
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (distanceY > 10) {
                tempo = tempo + 1;
                if (tempo < 300) {
                    tempoText.setText("" + tempo);
                } else {
                    tempo = 299;
                }
            } else if (distanceY < -10) {
                tempo = tempo - 1;
                if (tempo > 0) {
                    tempoText.setText("" + tempo);
                } else {
                    tempo = 1;
                }
            } else if (distanceY > 0) {
                tempoDelay = tempoDelay + 1;
                if (tempoDelay >= 10) {
                    tempoDelay = 0;
                    tempo = tempo + 1;
                    if (tempo < 300) {
                        tempoText.setText("" + tempo);
                    } else {
                        tempo = 299;
                    }
                }
            } else if (distanceY < 0) {
                tempoDelay = tempoDelay - 1;
                if (tempoDelay <= 0) {
                    tempoDelay = 10;
                    tempo = tempo - 1;
                    if (tempo > 0) {
                        tempoText.setText("" + tempo);
                    } else {
                        tempo = 1;
                    }
                }
            }
            tempoMS = (int) ((60000 / tempo) * (100 / (double) (pourcentage)));

            if (tempo < 20) {
                tempoName.setText("Larghissimo");
            } else if (tempo >= 20 && tempo < 40) {
                tempoName.setText("Grave");
            } else if (tempo >= 40 && tempo < 60) {
                tempoName.setText("Lento/Largo");
            } else if (tempo >= 60 && tempo < 66) {
                tempoName.setText("Larghetto");
            } else if (tempo >= 66 && tempo < 76) {
                tempoName.setText("Adagio");
            } else if (tempo >= 76 && tempo < 108) {
                tempoName.setText("Andante");
            } else if (tempo >= 108 && tempo < 120) {
                tempoName.setText("Moderato");
            } else if (tempo >= 120 && tempo < 140) {
                tempoName.setText("Allegro");
            } else if (tempo >= 140 && tempo < 168) {
                tempoName.setText("Vivace");
            } else if (tempo >= 168 && tempo < 200) {
                tempoName.setText("Presto");
            } else if (tempo >= 200) {
                tempoName.setText("Prestissimo");
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

    }
    private class GestureListenerRythm extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            mHandler.removeCallbacks(metronomeClick);
            if(t2.isAlive()) {
                t2.interrupt();
            }
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            startStopCount();

            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (state) {
                if (distanceY > 20) {
                    pourcentage = pourcentage + 1;
                    if (pourcentage <= 100) {
                        pourcentageText.setText("" + pourcentage + "%");
                    } else {
                        pourcentage = 100;
                    }
                } else if (distanceY < -20) {
                    pourcentage = pourcentage - 1;
                    if (pourcentage > 0) {
                        pourcentageText.setText("" + pourcentage + "%");
                    } else {
                        pourcentage = 1;
                    }
                } else if (distanceY > 0) {
                    pourcentageDelay = pourcentageDelay + 1;
                    if (pourcentageDelay >= 10) {
                        pourcentageDelay = 0;
                        pourcentage = pourcentage + 1;
                        if (pourcentage <= 100) {
                            pourcentageText.setText("" + pourcentage + "%");
                        } else {
                            pourcentage = 100;
                        }
                    }
                } else if (distanceY < 0) {
                    pourcentageDelay = pourcentageDelay - 1;
                    if (pourcentageDelay <= 0) {
                        pourcentageDelay = 10;
                        pourcentage = pourcentage - 1;
                        if (pourcentage > 0) {
                            pourcentageText.setText("" + pourcentage + "%");
                        } else {
                            pourcentage = 1;
                        }
                    }
                }
            } else {
                if (distanceY > 20) {
                    rythm1 = rythm1 + 1;
                    if (rythm1 < 64) {
                        rythm1Text.setText("" + rythm1);
                    } else {
                        rythm1 = 64;
                    }
                } else if (distanceY < -20) {
                    rythm1 = rythm1 - 1;
                    if (rythm1 > 0) {
                        rythm1Text.setText("" + rythm1);
                    } else {
                        rythm1 = 1;
                    }
                } else if (distanceY > 0) {
                    rythm1Delay = rythm1Delay + 1;
                    if (rythm1Delay >= 10) {
                        rythm1Delay = 0;
                        rythm1 = rythm1 + 1;
                        if (rythm1 < 64) {
                            rythm1Text.setText("" + rythm1);
                        } else {
                            rythm1 = 64;
                        }
                    }
                } else if (distanceY < 0) {
                    rythm1Delay = rythm1Delay - 1;
                    if (rythm1Delay <= 0) {
                        rythm1Delay = 10;
                        rythm1 = rythm1 - 1;
                        if (rythm1 > 0) {
                            rythm1Text.setText("" + rythm1);
                        } else {
                            rythm1 = 1;
                        }
                    }
                }
            }
            tempoMS = (int) ((60000 / tempo) * (100 / (double) (pourcentage)));
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

}
