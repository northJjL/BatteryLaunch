# BatteryLaunch

仿魅族手机的充电图形


<img src="https://github.com/northJjL/BatteryLaunch/blob/master/BatteryLaunch.gif" width="320" alt="gif">

### 布局

```xml
    <com.jjl.demo.batterylaunch.BatteryView
        android:id="@+id/battery_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        app:bubbleNumber="5"
        />
```

### 代码调用

```java
	mBatteryView.setPower(level);//设置当前电量（x/100）
	mBatteryView.startAnim();
	mBatteryView.stopAnim();
```

### 布局属性
		
		//泡泡 数量		
		<attr name="bubbleNumber" format="integer" />
		//泡泡 最小大小
		<attr name="bubbleFixSize" format="integer" />
		//泡泡 随机增大的范围
		<attr name="bubbleRandomSize" format="integer" />
		//泡泡 最小X移动大小
		<attr name="fixMoveX" format="integer" />
		//泡泡 随机增大X移动范围
		<attr name="randomMoveX" format="integer" />
		//泡泡 最小透明度
		<attr name="fixAlpha" format="integer" />
		//泡泡 随机增加的透明度范围
		<attr name="randomAlpha" format="integer" />
		//泡泡 最小上升时间
		<attr name="fixTime" format="integer" />
		//泡泡 随机增加上升时间范围
		<attr name="randomTime" format="integer" />
		//泡泡 上升到电池的距离
		<attr name="papawUpPathSize" format="float" />

