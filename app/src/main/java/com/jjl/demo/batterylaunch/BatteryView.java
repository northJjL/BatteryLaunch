package com.jjl.demo.batterylaunch;

import java.util.Random;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.AttributeSet;
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
	private int mFixSize  = 9;//圆圈固定最小值
	private int mRandomSize  = 9;//圆圈随机的范围值
	private int mRandomMoveX = 80;//随机移动x的范围值(/100)
	private int mFixMoveX = 60;//固定移动x的最小值(/100)
	private int mRandomAlpha = 80;//随机透明度
	private int mFixAlpha = 120;//固定最小透明度
	private int mRandomTime = 4500;//随机时间值
	private int mFixTime = 2500;//固定时间最小值
	private float mPapawUpPathSize = 220f; // 气泡可上升的高度

	private float [] curY= new float[mBubbleNum];//当前y的位置
	private float [] curX= new float[mBubbleNum];//当前x的位置
	private int [] mRadius= new int[mBubbleNum];//圆圈的半径
	private int [] mAlpha= new int[mBubbleNum];//圆圈的透明度
	private float [] mXMove= new float[mBubbleNum];//x的移动速度
//	private TimeCount [] mTimeCount= new TimeCount[mBubbleNum];//计时器
	private int [] mTime= new int[mBubbleNum];//上升时长

	private AnimatorUpdate [] mAnimatorUpdate= new AnimatorUpdate[mBubbleNum];//上升时长
	private float [] mXLimit= new float[mBubbleNum];//X限制的位置

	public BatteryView(Context context) {
		super(context);
	}

	public BatteryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		// 屏幕密度适配
		initBatterySize();

		//初始化 随机
		mRandom = new Random();

		//测量的宽高
		MeasureBatteryWidth = (int) (mBatteryWidth + mBatteryStroke);
		MeasureBatteryHeight = (int) (mBatteryHeight + (mCapHeight - mBatteryStroke / 2) + mPapawUpPathSize);

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
		mFixSize = (int) performDensityChange(context,mFixSize);
		mRandomSize = (int) performDensityChange(context,mRandomSize);
		mRandomMoveX = (int) performDensityChange(context, mRandomMoveX);
		mFixMoveX = (int) performDensityChange(context, mFixMoveX);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(MeasureBatteryWidth, MeasureBatteryHeight);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save();

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
		mRadius[bubbleNum] = mRandom.nextInt(mRandomSize) + mFixSize;
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
		if (mPower >= 100) {
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
			if(value > 0.8f){
				changeOrientation(xLeftLimitGather, xRightLimitGather);
			}else{
				changeOrientation(xLeftLimit, xRightLimit);
			}

			curY[type] = mBatteryRect.bottom + mPapawUpPathSize - (mPapawUpPathSize * value);
			if(value == 1f){
				handler.sendEmptyMessage(type);
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
