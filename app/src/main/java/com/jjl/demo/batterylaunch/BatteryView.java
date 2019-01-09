package com.jjl.demo.batterylaunch;

import java.nio.file.attribute.AttributeView;
import java.text.DecimalFormat;
import java.util.Random;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class BatteryView extends View{

	private Context context;

	private boolean isRunning = false;//是否启动动画
	private int mDefaultStrokeWidth = 1;

	private int MeasureBatteryHeight;//测量电池的总高度
	private int MeasureBatteryWidth;//测量电池的总宽度

	private float mPower = 0f; // 电量
	private float mBatteryStroke = 8; // 线边款的宽度

	private float mBatteryWidth = 220f; // 电池的宽度
	private float mBatteryHeight = 380f; // 电池的高度
	private float mBatteryRadius = 25f;// 电池外核半径

	private float mCapWidth = 60f;//电池盖的宽
	private float mCapHeight = 22f;//电池盖的高
	private float mCapRadius = 180;// 电池盖半径

	private float mPowerPadding = 4f;//电量离边框的间距
	private float mPowerRadius = 20f;// 电量内核半径
	private float mPowerHeight = mBatteryHeight - mBatteryStroke / 2 - mPowerPadding * 2 ;// 电池身体的总高度

	private Paint mPaint;
	private RectF mBatteryRect;
	private RectF mCapRect;
	private RectF mPowerRect;

	private Random mRandom;
	private int mBubbleNum = 5;//圆圈的数量
	private int mBubbleFixSize = 9;//圆圈固定最小值
	private int mBubbleRandomSize = 9;//圆圈随机的范围值
	private int mFixMoveX = 60;//固定移动x的最小值(/100)
	private int mRandomMoveX = 80;//随机移动x的范围值(/100)
	private int mFixAlpha = 120;//固定最小透明度
	private int mRandomAlpha = 80;//随机透明度
	private int mFixTime = 2500;//固定时间最小值
	private int mRandomTime = 4500;//随机时间值
	private float mPapawUpPathSize = 220f; // 气泡可上升的高度

	private float [] curY= new float[mBubbleNum];//当前圆圈y的位置
	private float [] curX= new float[mBubbleNum];//当前圆圈x的位置
	private int [] mRadius= new int[mBubbleNum];//圆圈的半径
	private int [] mAlpha= new int[mBubbleNum];//圆圈的透明度
	private float [] mXMove= new float[mBubbleNum];//x的移动速度
//	private TimeCount [] mTimeCount= new TimeCount[mBubbleNum];//计时器
	private int [] mTime= new int[mBubbleNum];//上升时长

	private AnimatorUpdate [] mAnimatorUpdate= new AnimatorUpdate[mBubbleNum];//ValueAnimator
	private float [] mXLimit= new float[mBubbleNum];//圆圈X轴限制
	private int mWidthSize = 0;//布局给予宽度
	private int mHeightSize = 0;//布局给予高度
	private int MeasureMode = -1;//测量模式 0、默认值 1、测高 2、测宽 3、测宽高
	private int mPaddingBottom;
	private int mPaddingTop;
	private int mPaddingLeft;
	private int mPaddingRight;

	public BatteryView(Context context) {
		super(context);
		initBattery(context ,null);
	}

	public BatteryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initBattery(context ,attrs);
	}

	public BatteryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initBattery(context ,attrs);
	}

	private void initBattery(Context context ,AttributeSet attrs) {
		this.context = context;
		if(attrs != null){
			TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BatteryView);
			int bubbleNumber = typedArray.getInt(R.styleable.BatteryView_bubbleNumber,-1);
			if(bubbleNumber!=-1) mBubbleNum =bubbleNumber;

			int bubbleFixSize = typedArray.getInt(R.styleable.BatteryView_bubbleFixSize,-1);
			if(bubbleFixSize!=-1)mBubbleFixSize =bubbleFixSize;

			int bubbleRandomSize = typedArray.getInt(R.styleable.BatteryView_bubbleRandomSize,-1);
			if(bubbleRandomSize!=-1)mBubbleRandomSize =bubbleRandomSize;

			int fixMoveX = typedArray.getInt(R.styleable.BatteryView_fixMoveX,-1);
			if(fixMoveX!=-1)mFixMoveX =fixMoveX;

			int randomMoveX = typedArray.getInt(R.styleable.BatteryView_randomMoveX,-1);
			if(randomMoveX!=-1)mRandomMoveX =randomMoveX;

			int fixAlpha = typedArray.getInt(R.styleable.BatteryView_fixAlpha,-1);
			if(fixAlpha!=-1)mFixAlpha =fixAlpha;

			int randomAlpha = typedArray.getInt(R.styleable.BatteryView_randomAlpha,-1);
			if(randomAlpha!=-1)mRandomAlpha =randomAlpha;

			int fixTime = typedArray.getInt(R.styleable.BatteryView_fixTime,-1);
			if(fixTime!=-1)mFixTime =fixTime;

			int randomTime = typedArray.getInt(R.styleable.BatteryView_randomTime,-1);
			if(randomTime!=-1)mRandomTime =randomTime;

			float papawUpPathSize = typedArray.getFloat(R.styleable.BatteryView_papawUpPathSize,-1f);
			if(papawUpPathSize!=-1f)mPapawUpPathSize =papawUpPathSize;

            typedArray.recycle();
		}
		mPaddingBottom = getPaddingBottom();
		mPaddingTop = getPaddingTop();
		mPaddingLeft = getPaddingLeft();
		mPaddingRight = getPaddingRight();

		//初始化 泡泡参数
		initBubbleParameter();

		// 屏幕密度适配
		initBatterySize();

		//初始化 随机
		mRandom = new Random();

		//测量的宽高
		MeasureBatteryWidth = (int) (mBatteryWidth + mBatteryStroke) ;
		MeasureBatteryHeight = (int) (mBatteryHeight + (mCapHeight + mBatteryStroke*3/2) +mPapawUpPathSize );

		//初始化 笔
		mPaint = new Paint();
		mPaint.setAntiAlias(true);

		//初始化 电池边框
		mBatteryRect = new RectF(); // 电量边款
		mBatteryRect.left = mBatteryStroke / 2;
		mBatteryRect.top = mCapHeight ;
		mBatteryRect.right = mBatteryWidth + mBatteryStroke/2;
		mBatteryRect.bottom = mBatteryHeight + mCapHeight + mBatteryStroke;

		//初始化 电池帽子
		mCapRect = new RectF(); // 电量帽子
		mCapRect.top = mBatteryStroke/2;
		mCapRect.left = (mBatteryWidth - mCapWidth) / 2 + mBatteryStroke/2;
		mCapRect.bottom = mCapHeight + mBatteryStroke/2;
		mCapRect.right = mBatteryRect.right/2 + mCapWidth/2;

		//初始化 电量
		mPowerRect = new RectF();// 电量填充
		mPowerRect.top = mBatteryRect.top + mBatteryStroke/2 +mPowerPadding + mPowerHeight * ((100f - mPower) / 100f);
		mPowerRect.left = mBatteryRect.left + mBatteryStroke/2 + mPowerPadding ;
		mPowerRect.bottom = mBatteryRect.bottom  - mBatteryStroke/2 - mPowerPadding;
		mPowerRect.right = mBatteryRect.right - mBatteryStroke/2 - mPowerPadding ;
	}

	private void initBubbleParameter() {
		curY= new float[mBubbleNum];//当前圆圈y的位置
		curX= new float[mBubbleNum];//当前圆圈x的位置
		mRadius= new int[mBubbleNum];//圆圈的半径
		mAlpha= new int[mBubbleNum];//圆圈的透明度
		mXMove= new float[mBubbleNum];//x的移动速度
		mTime= new int[mBubbleNum];//上升时长
//		mTimeCount= new TimeCount[mBubbleNum];//计时器
		mAnimatorUpdate= new AnimatorUpdate[mBubbleNum];//ValueAnimator
		mXLimit= new float[mBubbleNum];//圆圈X轴限制
	}

	/**
	 * 按3.0密度的大小显示电池
	 */
	private void initBatterySize() {
		mBatteryWidth = performDensityChange(context,mBatteryWidth);
		mBatteryHeight = performDensityChange(context,mBatteryHeight);
		mBatteryStroke = performDensityChange(context,mBatteryStroke);
		mCapWidth = performDensityChange(context,mCapWidth);
		mCapHeight = performDensityChange(context,mCapHeight);
		mPowerPadding = performDensityChange(context,mPowerPadding);
		mPowerHeight = performDensityChange(context,mPowerHeight);
		mPapawUpPathSize = performDensityChange(context, mPapawUpPathSize);
		mCapRadius = performDensityChange(context,mCapRadius);
		mBatteryRadius = performDensityChange(context,mBatteryRadius);
		mPowerRadius = performDensityChange(context,mPowerRadius);
		mBubbleFixSize = (int) performDensityChange(context, mBubbleFixSize);
		mBubbleRandomSize = (int) performDensityChange(context, mBubbleRandomSize);
		mRandomMoveX = (int) performDensityChange(context, mRandomMoveX);
		mFixMoveX = (int) performDensityChange(context, mFixMoveX);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mWidthSize = MeasureSpec.getSize(widthMeasureSpec);
		int WidthMode = MeasureSpec.getMode(widthMeasureSpec);
		mHeightSize = MeasureSpec.getSize(heightMeasureSpec);
		int HeightMode = MeasureSpec.getMode(heightMeasureSpec);
		// TODO WidthMode 不知什么原因，布局是wrap_content的情况下，
		// TODO 会在第二次赋值的时候spec返回mode为EXACTLY ,MeasureMode=1存在问题
		if(WidthMode == MeasureSpec.AT_MOST && HeightMode == MeasureSpec.AT_MOST){
			MeasureMode = 0;
			setMeasuredDimension(MeasureBatteryWidth + mPaddingRight + mPaddingLeft, MeasureBatteryHeight + mPaddingTop + mPaddingBottom);
		}else if(WidthMode == MeasureSpec.AT_MOST ){
			MeasureMode = 1;
			setMeasuredDimension(MeasureBatteryWidth + mPaddingRight + mPaddingLeft, mHeightSize);
		}else if(HeightMode == MeasureSpec.AT_MOST ){
			MeasureMode = 2;
			setMeasuredDimension(mWidthSize, MeasureBatteryHeight + mPaddingTop + mPaddingBottom);
		}else{
			MeasureMode = 3;
			setMeasuredDimension(mWidthSize, mHeightSize);
		}
	}


	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left , top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		//绘制大小
		float heightScale = (float)(mHeightSize - mPaddingTop - mPaddingBottom)/MeasureBatteryHeight;
		float widthScale = (float)(mWidthSize - mPaddingRight - mPaddingLeft)/MeasureBatteryWidth;
		canvas.translate(mPaddingLeft ,mPaddingTop);
		switch (MeasureMode){
			case 0:
				canvas.scale(1, 1);
				break;
			case 1:
				canvas.scale(1, heightScale);
				break;
			case 2:
				canvas.scale(widthScale, 1);
				break;
			case 3:
				canvas.scale(widthScale ,heightScale);
				break;
		}

		// 绘制电池的边框
		mPaint.setColor(Color.GRAY);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(mBatteryStroke);
		canvas.drawRoundRect(mBatteryRect, mBatteryRadius, mBatteryRadius, mPaint);

		//绘制电池盖子
		mPaint.setStyle(Style.FILL);
		canvas.drawRoundRect(mCapRect, mCapRadius, mCapRadius, mPaint);

		// 填充电量的颜色
		mPaint.setStrokeWidth(mDefaultStrokeWidth);
		if (mPower > 30) {
			mPaint.setColor(Color.GREEN);
		} else if (mPower > 10) {
			mPaint.setColor(Color.YELLOW);
		} else {
			mPaint.setColor(Color.RED);
		}

		//绘制电量
		canvas.drawRoundRect(mPowerRect, mPowerRadius, mPowerRadius, mPaint);

		if (isRunning) {
			//绘制圆圈
			for (int i  = 0 ; i < mBubbleNum ; i++){
				mPaint.setAlpha(mAlpha[i]);
				canvas.drawCircle(curX[i], curY[i], mRadius[i], mPaint);
			}
		}
		canvas.restore();
	}

	private void create(int bubbleNum) {
		mRadius[bubbleNum] = mRandom.nextInt(mBubbleRandomSize) + mBubbleFixSize;
		mXMove[bubbleNum] = (mRandom.nextInt(mRandomMoveX) + mFixMoveX) /100f;
		mAlpha[bubbleNum] = mRandom.nextInt(mRandomAlpha) + mFixAlpha;
		mXLimit[bubbleNum] = mRandom.nextInt(120) + MeasureBatteryWidth - 160;
		mTime[bubbleNum] = mRandom.nextInt(mRandomTime) + mFixTime;
		curX[bubbleNum] = MeasureBatteryWidth/2;
		curY[bubbleNum] = mBatteryRect.bottom + mPapawUpPathSize;
//		if(mTimeCount[bubbleNum] != null)
//			mTimeCount[bubbleNum].cancel();
//		mTimeCount[bubbleNum]  = new TimeCount(mTime[bubbleNum] , 50, bubbleNum);
//		mTimeCount[bubbleNum].start();
		mAnimatorUpdate[bubbleNum] = new AnimatorUpdate(bubbleNum);
		ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
		//减速插值器
		animator.setInterpolator(new DecelerateInterpolator());
		animator.addUpdateListener(mAnimatorUpdate[bubbleNum]);
		animator.setDuration(mTime[bubbleNum]);
		animator.start();
	}

	public void setPower(float power) {
		// 充电的时候调用
		mPower = power;
		if (mPower >= 100) {
			mPower = 100;
		} else if (mPower < 10) {
			mPower = 10;
		}
		mPowerRect.top = mBatteryRect.top + mBatteryStroke/2 +mPowerPadding + mPowerHeight * ((100f - mPower) / 100f);
		if (mPower > 100) {
			stopAnim();
		} else {
			startAnim();
		}
		invalidate();
	}

	public void startAnim() {
		if (!isRunning) {
			isRunning = true;
			for (int i  = 0 ; i < mBubbleNum ; i++){
				create(i);
			}
		}
	}

	public void stopAnim() {
		if (isRunning) {
			isRunning = false;
//			for (int i  = 0 ; i < mBubbleNum ; i++){
//				mTimeCount[i].cancel();
//			}
			invalidate();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		isRunning = false;
		handler.removeCallbacksAndMessages(null);
		handler = null;
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			create(msg.what);
			invalidate();
		};
	};

	class AnimatorUpdate implements ValueAnimator.AnimatorUpdateListener {

		private final int type;
		private boolean isRight = true;

		public AnimatorUpdate(int type) {
			this.type = type;
			//初始化方向
			this.isRight = type % 2==0;
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			float value = (float) animation.getAnimatedValue();
			float xLeftLimit = MeasureBatteryWidth/2 - mXLimit[type]/2;
			float xRightLimit = MeasureBatteryWidth/2 + mXLimit[type]/2;
			float xLeftLimitGather = MeasureBatteryWidth/2 - mXLimit[type]/4;
			float xRightLimitGather = MeasureBatteryWidth/2 + mXLimit[type]/4;
			//透明度变化
			if(value > 0.5f && mAlpha[type] > 0){
				mAlpha[type] -= 0.3f;
			}
			//集合
			if(value > 0.85f){
				changeOrientation(xLeftLimitGather, xRightLimitGather);
			}else{
				changeOrientation(xLeftLimit, xRightLimit);
			}

			curY[type] = mBatteryRect.bottom + mPapawUpPathSize - (mPapawUpPathSize * value);
			if(value == 1f && isRunning){
//				handler.sendEmptyMessage(type);
				post(new Runnable() {
					@Override
					public void run() {
						create(type);
						invalidate();
					}
				});
			}
			invalidate();
		}

		/**
		 * 根据限制的x具体，判断该移动的距离
		 */
		private void changeOrientation(float xLeftLimit, float xRightLimit) {
			if(curX[type] <  xLeftLimit){
                isRight = true;
            }else if(curX[type] > xRightLimit){
                isRight = false;
            }
			if(isRight){
                curX[type] += mXMove[type];
            }else{
                curX[type] -= mXMove[type];
            }
		}
	}

	/**
	 * 倒计时
	 */
	class TimeCount extends CountDownTimer {
		private int type;
		// 倒计时设置
		public TimeCount(long millisInFuture, long countDownInterval, int type) {
			super(millisInFuture, countDownInterval); // 参数依次为总时长,和计时的时间间隔
			this.type = type;
		}
		@Override
		public void onFinish() { // 计时完毕时触发
			handler.sendEmptyMessage(type);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// 计时过程显示 五种模式
			// 1、右 - 左 - 左 - 右 - 右
			// 2、右 - 右 - 左 - 左 - 右
			// 3、左 - 右 - 右 - 左 - 左
			// 4、左 - 左 - 右 - 右 - 左
			switch (type % 4) {
			case 0:
				if (millisUntilFinished > mTime[type] / 5 * 4) {
					curX[type] += mXMove[type];
				} else if (millisUntilFinished > mTime[type] / 5 * 3) {
					curX[type] -= mXMove[type];
				} else if (millisUntilFinished > mTime[type] / 5 * 2) {
					curX[type] -= mXMove[type];
					performDescending(type);
				} else if (millisUntilFinished > mTime[type] / 5 * 1) {
					curX[type] += mXMove[type];
					performDescending(type);
				} else if (millisUntilFinished > 0) {
					curX[type] += mXMove[type];
					performDescending(type);
				}
				curY[type] = mBatteryRect.bottom + mPapawUpPathSize - (mPapawUpPathSize * (1 - millisUntilFinished / (float) mTime[type]));
				break;
			case 1:
				if (millisUntilFinished > mTime[type] / 5 * 4) {
					curX[type] += mXMove[type];
				} else if (millisUntilFinished > mTime[type] / 5 * 3) {
					curX[type] += mXMove[type];
				} else if (millisUntilFinished > mTime[type] / 5 * 2) {
					curX[type] -= mXMove[type];
					performDescending(type);
				} else if (millisUntilFinished > mTime[type] / 5 * 1) {
					curX[type] -= mXMove[type];
					performDescending(type);
				} else if (millisUntilFinished > 0) {
					curX[type] += mXMove[type];
					performDescending(type);
				}
				curY[type] = mBatteryRect.bottom + mPapawUpPathSize - (mPapawUpPathSize * (1 - millisUntilFinished / (float) mTime[type]));
				break;
			case 2:
				if (millisUntilFinished > mTime[type] / 5 * 4) {
					curX[type] -= mXMove[type];
				} else if (millisUntilFinished > mTime[type] / 5 * 3) {
					curX[type] += mXMove[type];
				} else if (millisUntilFinished > mTime[type] / 5 * 2) {
					curX[type] += mXMove[type];
					performDescending(type);
				} else if (millisUntilFinished > mTime[type] / 5 * 1) {
					curX[type] -= mXMove[type];
					performDescending(type);
				} else if (millisUntilFinished > 0) {
					curX[type] -= mXMove[type];
					performDescending(type);
				}
				curY[type] = mBatteryRect.bottom + mPapawUpPathSize - (mPapawUpPathSize * (1 - millisUntilFinished / (float) mTime[type]));
				break;
			case 3:
				if (millisUntilFinished > mTime[type] / 5 * 4) {
					curX[type] -= mXMove[type];
				} else if (millisUntilFinished > mTime[type] / 5 * 3) {
					curX[type] -= mXMove[type];
				} else if (millisUntilFinished > mTime[type] / 5 * 2) {
					curX[type] += mXMove[type];
					performDescending(type);
				} else if (millisUntilFinished > mTime[type] / 5 * 1) {
					curX[type] += mXMove[type];
					performDescending(type);
				} else if (millisUntilFinished > 0) {
					curX[type] -= mXMove[type];
					performDescending(type);
				}
				curY[type] = mBatteryRect.bottom + mPapawUpPathSize - (mPapawUpPathSize * (1 - millisUntilFinished / (float) mTime[type]));
				break;
			}
			invalidate();
		}

		private void performDescending(int type) {
			mAlpha[type] -= 3;
			if(mAlpha[type] <= 0)
				mAlpha[type] = 0;
		}
	}

	/**
	 * density为3.0开发的大小，对应density显示不同的size
	 */
	public static float performDensityChange(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return  (dpValue / 3.0f *  scale + 0.5f);
	}
}
