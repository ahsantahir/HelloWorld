package com.example.sabeeh.helloworld.entites;

/**
 * Represents an item in a swim_record
 */
public class swim_record {




    @com.google.gson.annotations.SerializedName("id")
    private String mId;
    @com.google.gson.annotations.SerializedName("type")
    private String mType;
    @com.google.gson.annotations.SerializedName("user_log")
    private String mUser;
    @com.google.gson.annotations.SerializedName("freq")
    private Number mFreq;
    @com.google.gson.annotations.SerializedName("coordina")
    private Number mCoordina;
    @com.google.gson.annotations.SerializedName("tot_time")
    private Number mTot_time;
    @com.google.gson.annotations.SerializedName("tot_distance")
    private Number mTot_distance;
    @com.google.gson.annotations.SerializedName("stroke_each_pool")
    private Number mStroke_each_pool;



    @com.google.gson.annotations.SerializedName("tot_stroke")
    private Number mTot_stroke;
    @com.google.gson.annotations.SerializedName("split")
    private Number mSplit;
    @com.google.gson.annotations.SerializedName("timing_turn")
    private Number mTiming_turn;
    @com.google.gson.annotations.SerializedName("cycle_rate_l")
    private Number mCycle_rate_l;
    @com.google.gson.annotations.SerializedName("cycle_rate_r")
    private Number mCycle_rate_r;
    @com.google.gson.annotations.SerializedName("mean_velocity")
    private Number mMean_velocity;
    @com.google.gson.annotations.SerializedName("stroke_length")
    private Number mStroke_length;
    @com.google.gson.annotations.SerializedName("stroke_freq")
    private Number mStroke_freq;
    @com.google.gson.annotations.SerializedName("roll_peaks")
    private Number mRoll_peaks;
    @com.google.gson.annotations.SerializedName("mean_roll_dx")
    private Number mMean_roll_dx;
    @com.google.gson.annotations.SerializedName("mean_roll_sx")
    private Number mMean_roll_sx;
    @com.google.gson.annotations.SerializedName("std_roll_dx")
    private Number mStd_roll_dx;
    @com.google.gson.annotations.SerializedName("std_roll_sx")
    private Number mStd_roll_sx;
    @com.google.gson.annotations.SerializedName("mean_roll")
    private Number mMean_roll;
    @com.google.gson.annotations.SerializedName("Std_roll")
    private Number mStd_roll;
    @com.google.gson.annotations.SerializedName("mean_pitch")
    private Number mMean_pitch;
    @com.google.gson.annotations.SerializedName("std_pitch")
    private Number mStd_pitch;
    @com.google.gson.annotations.SerializedName("clean_stroke_time")
    private Number mClean_stroke_time;
    @com.google.gson.annotations.SerializedName("errore")
    private Number mErrore;
    @com.google.gson.annotations.SerializedName("fatal_error")
    private Number mFatal_error;


    /**
     * ToDoItem constructor
     */
    public swim_record() {

    }




    public swim_record( String Id,String Type,String User,Number Freq,Number Coordina,Number Tot_time,Number Tot_distance,Number Stroke_each_pool,Number tot_stroke,Number Split,
                        Number Timing_turn,Number Cycle_rate_l,Number Cycle_rate_r,Number Mean_velocity,Number Stroke_length,Number Stroke_freq,
                        Number Roll_peaks,Number Mean_roll_dx,Number Mean_roll_sx,Number Std_roll_dx,Number Std_roll_sx,Number Mean_roll,Number Std_roll,
                        Number Mean_pitch,Number Std_pitch,Number Clean_stroke_time,Number Errore,Number Fatal_error ) {
      this.setId(Id);
        this.setmType(Type);
        this.setmUser(User);
        this.setmFreq(Freq);
        this.setmCoordina(Coordina);
        this.setmTot_time(Tot_time);
        this.setmTot_distance(Tot_distance);
        this.setmStroke_each_pool(Stroke_each_pool);
        this.setmTot_stroke(tot_stroke);
        this.setmSplit(Split);
        this.setmTiming_turn(Timing_turn);
        this.setmCycle_rate_l(Cycle_rate_l);
        this.setmCycle_rate_r(Cycle_rate_r);
        this.setmMean_velocity(Mean_velocity);
        this.setmStroke_length(Stroke_length);
        this.setmStroke_freq(Stroke_freq);
        this.setmRoll_peaks(Roll_peaks);
        this.setmMean_roll_dx(Mean_roll_dx);
        this.setmMean_roll_sx(Mean_roll_sx);
        this.setmStd_roll_dx(Std_roll_dx);
        this.setmStd_roll_sx(Std_roll_sx);
        this.setmMean_roll(Mean_roll);
        this.setmStd_roll(Std_roll);
        this.setmMean_pitch(Mean_pitch);
        this.setmStd_pitch(Std_pitch);
        this.setmClean_stroke_time(Clean_stroke_time);
        this.setmErrore(Errore);
        this.setmFatal_error(Fatal_error);

    }



    /**
     * Returns the item id
     */
    public String getId() {
        return mId;
    }

    /**
     * Sets the item id
     *
     * @param id
     *            id to set
     */
    public final void setId(String id) {
        mId = id;
    }

    /**
     * Indicates if the item is marked as completed
     */






    public Number getmFreq() {
        return mFreq;
    }

    public void setmFreq(Number mFreq) {
        this.mFreq = mFreq;
    }


    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }



    public Number getmCoordina() {
        return mCoordina;
    }

    public void setmCoordina(Number mCoordina) {
        this.mCoordina = mCoordina;
    }

    public Number getmTot_time() {
        return mTot_time;
    }

    public void setmTot_time(Number mTot_time) {
        this.mTot_time = mTot_time;
    }

    public Number getmTot_distance() {
        return mTot_distance;
    }

    public void setmTot_distance(Number mTot_distance) {
        this.mTot_distance = mTot_distance;
    }

    public Number getmStroke_each_pool() {
        return mStroke_each_pool;
    }

    public void setmStroke_each_pool(Number mStroke_each_pool) {
        this.mStroke_each_pool = mStroke_each_pool;
    }

    public Number getmSplit() {
        return mSplit;
    }

    public void setmSplit(Number mSplit) {
        this.mSplit = mSplit;
    }

    public Number getmTiming_turn() {
        return mTiming_turn;
    }

    public void setmTiming_turn(Number mTiming_turn) {
        this.mTiming_turn = mTiming_turn;
    }

    public Number getmCycle_rate_l() {
        return mCycle_rate_l;
    }

    public void setmCycle_rate_l(Number mCycle_rate_l) {
        this.mCycle_rate_l = mCycle_rate_l;
    }

    public Number getmCycle_rate_r() {
        return mCycle_rate_r;
    }

    public void setmCycle_rate_r(Number mCycle_rate_r) {
        this.mCycle_rate_r = mCycle_rate_r;
    }

    public Number getmMean_velocity() {
        return mMean_velocity;
    }

    public void setmMean_velocity(Number mMean_velocity) {
        this.mMean_velocity = mMean_velocity;
    }

    public Number getmStroke_length() {
        return mStroke_length;
    }

    public void setmStroke_length(Number mStroke_length) {
        this.mStroke_length = mStroke_length;
    }

    public Number getmStroke_freq() {
        return mStroke_freq;
    }

    public void setmStroke_freq(Number mStroke_freq) {
        this.mStroke_freq = mStroke_freq;
    }

    public Number getmRoll_peaks() {
        return mRoll_peaks;
    }

    public void setmRoll_peaks(Number mRoll_peaks) {
        this.mRoll_peaks = mRoll_peaks;
    }

    public Number getmMean_roll_dx() {
        return mMean_roll_dx;
    }

    public void setmMean_roll_dx(Number mMean_roll_dx) {
        this.mMean_roll_dx = mMean_roll_dx;
    }

    public Number getmMean_roll_sx() {
        return mMean_roll_sx;
    }

    public void setmMean_roll_sx(Number mMean_roll_sx) {
        this.mMean_roll_sx = mMean_roll_sx;
    }

    public Number getmStd_roll_dx() {
        return mStd_roll_dx;
    }

    public void setmStd_roll_dx(Number mStd_roll_dx) {
        this.mStd_roll_dx = mStd_roll_dx;
    }

    public Number getmStd_roll_sx() {
        return mStd_roll_sx;
    }

    public void setmStd_roll_sx(Number mStd_roll_sx) {
        this.mStd_roll_sx = mStd_roll_sx;
    }

    public Number getmMean_roll() {
        return mMean_roll;
    }

    public void setmMean_roll(Number mMean_roll) {
        this.mMean_roll = mMean_roll;
    }

    public Number getmStd_roll() {
        return mStd_roll;
    }

    public void setmStd_roll(Number mStd_roll) {
        this.mStd_roll = mStd_roll;
    }

    public Number getmMean_pitch() {
        return mMean_pitch;
    }

    public void setmMean_pitch(Number mMean_pitch) {
        this.mMean_pitch = mMean_pitch;
    }

    public Number getmStd_pitch() {
        return mStd_pitch;
    }

    public void setmStd_pitch(Number mStd_pitch) {
        this.mStd_pitch = mStd_pitch;
    }

    public Number getmClean_stroke_time() {
        return mClean_stroke_time;
    }

    public void setmClean_stroke_time(Number mClean_stroke_time) {
        this.mClean_stroke_time = mClean_stroke_time;
    }

    public Number getmErrore() {
        return mErrore;
    }

    public void setmErrore(Number mErrore) {
        this.mErrore = mErrore;
    }

    public Number getmFatal_error() {
        return mFatal_error;
    }

    public void setmFatal_error(Number mFatal_error) {
        this.mFatal_error = mFatal_error;
    }

    public String getmUser() {
        return mUser;
    }

    public void setmUser(String mUser) {
        this.mUser = mUser;
    }
    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }
    public Number getmTot_stroke() {
        return mTot_stroke;
    }

    public void setmTot_stroke(Number mTot_stroke) {
        this.mTot_stroke = mTot_stroke;
    }
    /**
     * Marks the item as completed or incompleted
     */


    @Override
    public boolean equals(Object o) {
        return o instanceof swim_record && ((swim_record) o).mId == mId;
    }
}