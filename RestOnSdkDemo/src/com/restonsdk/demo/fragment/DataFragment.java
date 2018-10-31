package com.restonsdk.demo.fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.restonsdk.demo.R;
import com.restonsdk.demo.bean.CvPoint;
import com.restonsdk.demo.util.DensityUtil;
import com.restonsdk.demo.util.HistoryDataComparator;
import com.restonsdk.demo.view.graphview.GraphView;
import com.restonsdk.demo.view.graphview.GraphViewSeries;
import com.restonsdk.demo.view.graphview.GraphViewStyle;
import com.restonsdk.demo.view.graphview.LineGraphView;
import com.restonsdk.demo.view.graphview.LineGraphView.BedBean;
import com.sleepace.sdk.core.heartbreath.domain.Analysis;
import com.sleepace.sdk.core.heartbreath.domain.Detail;
import com.sleepace.sdk.core.heartbreath.domain.HistoryData;
import com.sleepace.sdk.core.heartbreath.domain.Summary;
import com.sleepace.sdk.core.heartbreath.util.AnalysisUtil;
import com.sleepace.sdk.core.heartbreath.util.SleepConfig;
import com.sleepace.sdk.interfs.IConnectionStateCallback;
import com.sleepace.sdk.interfs.IDeviceManager;
import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CONNECTION_STATE;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.manager.DeviceType;
import com.sleepace.sdk.util.SdkLog;
import com.sleepace.sdk.util.TimeUtil;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DataFragment extends BaseFragment {

	private LayoutInflater inflater;
	private LinearLayout reportLayout;
	private Button btnAnalysis, btnShort, btnLong;
	private HistoryData shortData, longData;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	private DateFormat timeFormat = new SimpleDateFormat("HH:mm");
	private ProgressDialog progressDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		this.inflater = inflater;
		View view = inflater.inflate(R.layout.fragment_data, null);
//		SdkLog.log(TAG + " onCreateView-----------");
		findView(view);
		initListener();
		initUI();
		return view;
	}

	protected void findView(View root) {
		// TODO Auto-generated method stub
		super.findView(root);
		btnAnalysis = (Button) root.findViewById(R.id.btn_sleep_analysis);
		btnShort = (Button) root.findViewById(R.id.btn_sleep_short);
		btnLong = (Button) root.findViewById(R.id.btn_sleep_long);
		reportLayout = (LinearLayout) root.findViewById(R.id.layout_chart);
	}

	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		getRestonHelper().addConnectionStateCallback(stateCallback);
		btnAnalysis.setOnClickListener(this);
		btnShort.setOnClickListener(this);
		btnLong.setOnClickListener(this);
	}

	protected void initUI() {
		// TODO Auto-generated method stub
		mActivity.setTitle(R.string.report);
		btnAnalysis.setEnabled(getRestonHelper().isConnected());
		printLog(null);
		initDemoData();
		
		progressDialog = new ProgressDialog(mActivity);
		progressDialog.setIcon(android.R.drawable.ic_dialog_info);
		progressDialog.setMessage(getString(R.string.data_analyzed));
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
	}
	
	
	private IConnectionStateCallback stateCallback = new IConnectionStateCallback() {
		@Override
		public void onStateChanged(IDeviceManager manager, final CONNECTION_STATE state) {
			// TODO Auto-generated method stub
			
			if(!isAdded()){
				return;
			}
			
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(state == CONNECTION_STATE.DISCONNECT){
						btnAnalysis.setEnabled(false);
						printLog(R.string.connection_broken);
					}else if(state == CONNECTION_STATE.CONNECTED){
						
					}
				}
			});
		}
	};
	
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		getRestonHelper().removeConnectionStateCallback(stateCallback);
	}

	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		if (v == btnAnalysis) {
			progressDialog.show();
			printLog(R.string.data_analyzed);
			Calendar cal = Calendar.getInstance();
			int endTime = (int) (cal.getTimeInMillis() / 1000);
			cal.add(Calendar.DATE, -1);
			cal.set(Calendar.HOUR_OF_DAY, 20);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			int startTime = (int) (cal.getTimeInMillis() / 1000);
			getRestonHelper().historyDownload(0, endTime, 1, new IResultCallback<List<HistoryData>>() {
				@Override
				public void onResultCallback(final CallbackData<List<HistoryData>> cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							progressDialog.dismiss();
							if(checkStatus(cd)){
								List<HistoryData> list = cd.getResult();
								SdkLog.log(TAG+" historyDownload size:" + list.size());
								if(list.size() > 0){
									Collections.sort(list, new HistoryDataComparator());
									HistoryData historyData = list.get(0);
									SdkLog.log(TAG+" historyDownload first data duration:" + historyData.getSummary().getRecordCount()+",algorithmVer:" + historyData.getAnalysis().getAlgorithmVer());
									if(historyData.getAnalysis().getReportFlag() == 1){//长报告
										initLongReportView(historyData);
									}else{//短报告
										initShortReportView(historyData);
									}
								}else{
									printLog(R.string.no_data);
								}
							}else{
								SdkLog.log(TAG+" historyDownload fail cd:" + cd);
							}
						}
					});
				}
			});
		} else if (v == btnShort) {
			printLog(R.string.simulation_short_report);
			initShortReportView(shortData);
		} else if (v == btnLong) {
			printLog(R.string.simulation_long_report);
			initLongReportView(longData);
		}
	}

	
	private void initDemoData() {
		// TODO Auto-generated method stub
		shortData = createShortReportData(1483056407, 166);
		longData = createLongReportData(1500649565, 714);
	}

	private void initShortReportView(HistoryData historyData) {
		reportLayout.removeAllViews();
		
		String ver = null;
		Analysis analysis = historyData.getAnalysis();
		if(analysis != null){
			ver = analysis.getAlgorithmVer();
		}
		if(ver == null) ver = "";
		printLog(getString(R.string.generate_sleep_report, ver));
		
		View view = inflater.inflate(R.layout.layout_short_report, null);
		TextView tvCollectDate = (TextView) view.findViewById(R.id.tv_collect_date);
		TextView tvSleepTime = (TextView) view.findViewById(R.id.tv_sleep_time);
		TextView tvSleepDuration = (TextView) view.findViewById(R.id.tv_sleep_duration);
		TextView tvAvgHeartRate = (TextView) view.findViewById(R.id.tv_avg_heartrate);
		TextView tvAvgBreathRate = (TextView) view.findViewById(R.id.tv_avg_breathrate);
		if (analysis != null) {
			int starttime = historyData.getSummary().getStartTime();
			int endtime = starttime + historyData.getSummary().getRecordCount() * 60;
			tvCollectDate.setText(dateFormat.format(new Date(starttime * 1000l)));
			tvSleepTime.setText(timeFormat.format(new Date(starttime * 1000l)) + "(" + getString(R.string.starting_point) + ")-"
					+ timeFormat.format(new Date(endtime * 1000l)) + "(" + getString(R.string.end_point) + ")");
			int duration = historyData.getSummary().getRecordCount();
			int hour = duration / 60;
			int minute = duration % 60;
			tvSleepDuration.setText(hour + getString(R.string.unit_h) + minute + getString(R.string.unit_m));
			tvAvgHeartRate.setText(analysis.getAvgHeartRate() + getString(R.string.unit_heart));
			tvAvgBreathRate.setText(analysis.getAvgBreathRate() + getString(R.string.unit_respiration));
		}
		reportLayout.addView(view);
	}

	private void initLongReportView(HistoryData historyData) {
		reportLayout.removeAllViews();
		
		String ver = null;
		Analysis analysis = historyData.getAnalysis();
		if(analysis != null){
			ver = analysis.getAlgorithmVer();
		}
		if(ver == null) ver = "";
		printLog(getString(R.string.generate_sleep_report, ver));
		
		View view = inflater.inflate(R.layout.layout_long_report, null);
		LinearLayout mainGraph = (LinearLayout) view.findViewById(R.id.layout_chart);
		GraphView.GraphViewData[] mainData = getSleepGraphData(historyData.getDetail(), historyData.getAnalysis(), 60, DeviceType.DEVICE_TYPE_Z2);

		int think = (int) (DensityUtil.dip2px(mActivity, 1) * 0.8);
		final LineGraphView main_graph = new LineGraphView(mActivity, "");
		if (mainData == null) {
			mainData = new GraphView.GraphViewData[0];
		}

		GraphViewSeries series = new GraphViewSeries("", new GraphViewSeries.GraphViewSeriesStyle(getResources().getColor(R.color.COLOR_2), think), mainData);
		main_graph.addSeries(series);
		main_graph.isMySelft = true;
		if (mainData.length > 0) {
			main_graph.setViewPort(mainData[0].getX(), mainData[mainData.length - 1].getX());
		} else {
			main_graph.setViewPort(0, 10);
		}

		main_graph.setMinMaxY(-3, 2);
		main_graph.setVerticalLabels(
				new String[] { "", getString(R.string.wake_), getString(R.string.light_), getString(R.string.mid_), getString(R.string.deep_), "" });

		main_graph.setBeginAndOffset(historyData.getSummary().getStartTime(), TimeUtil.getTimeZoneSecond(), 0);
		main_graph.setScalable(false);
		main_graph.setScrollable(false);
		main_graph.setShowLegend(false);
		main_graph.setMainPoint(points);
		main_graph.setDrawBackground(true);
		main_graph.testVLabel = "wake";
		main_graph.setPauseData(apneaPauseList, heartPauseList);

		// 说明没有 数据
		if (mainData.length == 0) {
			main_graph.setHorizontalLabels(new String[] { "1", "2", "3", "4", "5", "6", "7" });
		}

		GraphViewStyle gvs = main_graph.getGraphViewStyle();
		gvs.setVerticalLabelsAlign(Paint.Align.CENTER);
		gvs.setTextSize(DensityUtil.sp2px(mActivity, 12));
		gvs.setGridColor(Color.parseColor("#668492a6"));
		gvs.setHorizontalLabelsColor(getResources().getColor(R.color.COLOR_3));
		gvs.setVerticalLabelsColor(getResources().getColor(R.color.COLOR_3));
		gvs.setLegendBorder(DensityUtil.dip2px(mActivity, 12));
		gvs.setNumVerticalLabels(4);
		gvs.setVerticalLabelsWidth(DensityUtil.dip2px(mActivity, 40));
		gvs.setNumHorizontalLabels(7);
		gvs.setLegendWidth(DensityUtil.dip2px(mActivity, 30));
		main_graph.setBedBeans(bedBeans);
		main_graph.setSleepUpIn(SleepInUP);
		mainGraph.removeAllViews();
		mainGraph.addView(main_graph);
		// main_graph.setOnHeartClickListener(heartClick);
//		main_graph.setOnGraphViewScrollListener(new GraphView.OnGraphViewScrollListener() {
//			@Override
//			public void onTouchEvent(MotionEvent event, GraphView v) {
//				main_graph.onMyTouchEvent(event);
//			}
//		});
		// main_graph.setTouchDisallowByParent(true);

		TextView tvCollectDate = (TextView) view.findViewById(R.id.tv_collect_date);
		TextView tvSleepScore = (TextView) view.findViewById(R.id.tv_sleep_score);
		LinearLayout layoutDeductionPoints = (LinearLayout) view.findViewById(R.id.layout_deduction_points);
		TextView tvSleepTime = (TextView) view.findViewById(R.id.tv_sleep_time);
		TextView tvSleepDuration = (TextView) view.findViewById(R.id.tv_sleep_duration);
		TextView tvAsleepDuration = (TextView) view.findViewById(R.id.tv_fall_asleep_duration);
		TextView tvAvgHeartRate = (TextView) view.findViewById(R.id.tv_avg_heartrate);
		TextView tvAvgBreathRate = (TextView) view.findViewById(R.id.tv_avg_breathrate);
		TextView tvBreathPause = (TextView) view.findViewById(R.id.tv_respiration_pause);
		TextView tvDeepSleepPer = (TextView) view.findViewById(R.id.tv_deep_sleep_proportion);
		TextView tvMidSleepPer = (TextView) view.findViewById(R.id.tv_medium_sleep_proportion);
		TextView tvLightSleepPer = (TextView) view.findViewById(R.id.tv_light_sleep_proportion);
		TextView tvWakeSleepPer = (TextView) view.findViewById(R.id.tv_Sober_proportion);
		TextView tvWakeTimes = (TextView) view.findViewById(R.id.tv_wake_times);
		TextView tvTurnTimes = (TextView) view.findViewById(R.id.tv_turn_times);
		TextView tvBodyTimes = (TextView) view.findViewById(R.id.tv_Body_times);
		TextView tvOutTimes = (TextView) view.findViewById(R.id.tv_out_times);

		if (analysis != null) {
			tvCollectDate.setText(dateFormat.format(new Date(historyData.getSummary().getStartTime() * 1000l)));
			tvSleepScore.setText(String.valueOf(analysis.getSleepScore()));

			int fallSleep = analysis.getFallsleepTimeStamp();
			int wakeUp = analysis.getWakeupTimeStamp();
			tvSleepTime.setText(timeFormat.format(new Date(fallSleep * 1000l)) + "(" + getString(R.string.asleep_point) + ")-"
					+ timeFormat.format(new Date(wakeUp * 1000l)) + "(" + getString(R.string.awake_point) + ")");
			int duration = analysis.getDuration();
			int hour = duration / 60;
			int minute = duration % 60;
			tvSleepDuration.setText(hour + getString(R.string.unit_h) + minute + getString(R.string.unit_m));
			tvAvgHeartRate.setText(analysis.getAvgHeartRate() + getString(R.string.unit_heart));
			tvAvgBreathRate.setText(analysis.getAvgBreathRate() + getString(R.string.unit_respiration));

			List<DeductItems> list = new ArrayList<DataFragment.DeductItems>();
			
			if (analysis.getMd_heart_high_decrease_scale() > 0 && analysis.getMd_heart_low_decrease_scale() > 0) {// 心率不齐
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.heartrate_not_near);
				item.score = analysis.getMd_heart_high_decrease_scale() + analysis.getMd_heart_low_decrease_scale();
				list.add(item);
			} else if (analysis.getMd_heart_high_decrease_scale() > 0) {// 心率过速
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.heartrate_too_fast);
				item.score = analysis.getMd_heart_high_decrease_scale();
				list.add(item);
			} else if (analysis.getMd_heart_low_decrease_scale() > 0) {// 心率过缓
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.heartrate_too_low);
				item.score = analysis.getMd_heart_low_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_breath_high_decrease_scale() > 0) {// 呼吸过速
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.deduction_breathe_fast);
				item.score = analysis.getMd_breath_high_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_breath_low_decrease_scale() > 0) {// 呼吸过缓
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.deduction_breathe_slow);
				item.score = analysis.getMd_breath_low_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_body_move_decrease_scale() != 0) {// 躁动不安
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.restless);
				item.score = analysis.getMd_body_move_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_leave_bed_decrease_scale() > 0) {// 起夜过多
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.up_night_more);
				item.score = analysis.getMd_leave_bed_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_sleep_time_increase_scale() > 0) {// 睡眠时间过长
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.actual_sleep_long);
				item.score = analysis.getMd_sleep_time_increase_scale();
				list.add(item);
			}

			if (analysis.getMd_sleep_time_decrease_scale() > 0) {// 睡眠时间过短
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.actual_sleep_short);
				item.score = analysis.getMd_sleep_time_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_perc_deep_decrease_scale() > 0) {// 深睡眠时间不足
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.deep_sleep_time_too_short);
				item.score = analysis.getMd_perc_deep_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_fall_asleep_time_decrease_scale() > 0) {// 入睡时间长
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.fall_asleep_hard);
				item.score = analysis.getMd_fall_asleep_time_decrease_scale();
				list.add(item);
			}

			if (analysis.getBreathPauseTimes() > 0 && analysis.getMd_breath_stop_decrease_scale() > 0) {// 呼吸暂停
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.abnormal_breathing);
				item.score = analysis.getMd_breath_stop_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_heart_stop_decrease_scale() > 0) {// 心跳骤停
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.heart_pause);
				item.score = analysis.getMd_heart_stop_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_start_time_decrease_scale() > 0) {// 上床时间较晚
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.start_sleep_time_too_latter);
				item.score = analysis.getMd_start_time_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_wake_cnt_decrease_scale() > 0) {// 清醒次数较多
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.wake_times_too_much);
				item.score = analysis.getMd_wake_cnt_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_perc_effective_sleep_decrease_scale() > 0) {// 良性睡眠扣分
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.benign_sleep);
				item.score = analysis.getMd_perc_effective_sleep_decrease_scale();
				list.add(item);
			}

			int size = list.size();
			if(size > 0){
				for(int i=0;i<size;i++){
					View pointView = inflater.inflate(R.layout.layout_deduction_points, null);
					TextView tvDesc = (TextView) pointView.findViewById(R.id.tv_deduction_desc);
					TextView tvScore = (TextView) pointView.findViewById(R.id.tv_deduction_score);
					tvDesc.setText((i+1)+"."+list.get(i).desc);
					tvScore.setText("-" + Math.abs(list.get(i).score));
					layoutDeductionPoints.addView(pointView);
				}
			}
			
			hour = analysis.getFallAlseepAllTime() / 60;
			minute = analysis.getFallAlseepAllTime() % 60;
			tvAsleepDuration.setText(hour + getString(R.string.unit_h) + minute + getString(R.string.unit_m));
			int idx = 0;
			StringBuffer sb = new StringBuffer();
			if (analysis.getBreathPauseTimes() > 0) {
				idx = 0;
				int stime = historyData.getSummary().getStartTime();
				Detail detail = historyData.getDetail();
				int[] status = detail.getStatus();
				int len = status.length;
				for (int i = 0; i < len; i++) {
					if (analysis.getBreathRateStatusAry()[i] > 0) {
						idx++;
						sb.append(getString(R.string.sequence, String.valueOf(idx)) + "\t\t\t");
						int time = stime + i * 60;
						sb.append(timeFormat.format(new Date(time * 1000l)) + "\t\t\t");
						sb.append(analysis.getBreathRateStatusAry()[i] + getString(R.string.unit_s) + "\n");
					}
				}

				if (sb.length() > 0) {
					sb.delete(sb.lastIndexOf("\n"), sb.length());
				}
				tvBreathPause.setText(sb.toString());
			} else {
				tvBreathPause.setText(R.string.nothing);
			}

			tvDeepSleepPer.setText(analysis.getDeepSleepPerc() + "%");
			tvMidSleepPer.setText(analysis.getInSleepPerc() + "%");
			tvLightSleepPer.setText(analysis.getLightSleepPerc() + "%");
			tvWakeSleepPer.setText(analysis.getWakeSleepPerc() + "%");
			tvWakeTimes.setText(analysis.getWakeTimes() + getString(R.string.unit_times));
			tvTurnTimes.setText(analysis.getTrunOverTimes() + getString(R.string.unit_times));
			tvBodyTimes.setText(analysis.getBodyMovementTimes() + getString(R.string.unit_times));
			tvOutTimes.setText(analysis.getLeaveBedTimes() + getString(R.string.unit_times));
		}

		reportLayout.addView(view);

	}
	
	
	class DeductItems {
		String desc;
		int score;
	}
	

	private HistoryData createShortReportData(int starttime, int count) {
		HistoryData historyData = new HistoryData();
		Summary summ = new Summary();
		summ.setStartTime(starttime);
		summ.setRecordCount(count);
		historyData.setSummary(summ);

		Detail detail = new Detail();
		int[] heartRate = new int[] { 60, 62, 64, 62, 64, 63, 66, 68, 68, 68, 68, 59, 64, 64, 65, 63, 67, 67, 67, 63, 69, 70, 71, 72, 68, 70, 72, 71, 71, 69,
				71, 66, 65, 67, 68, 65, 63, 62, 70, 66, 65, 57, 65, 66, 64, 68, 67, 66, 65, 67, 68, 66, 68, 68, 68, 66, 68, 66, 67, 67, 68, 67, 67, 67, 66, 68,
				67, 67, 67, 66, 67, 69, 69, 63, 73, 69, 74, 71, 72, 74, 74, 75, 74, 73, 73, 72, 76, 72, 70, 70, 72, 73, 73, 68, 70, 71, 66, 70, 74, 73, 76, 67,
				72, 71, 65, 65, 65, 71, 69, 64, 68, 65, 64, 67, 66, 61, 60, 65, 66, 68, 67, 60, 63, 64, 63, 66, 76, 76, 75, 79, 78, 67, 66, 67, 66, 70, 66, 64,
				66, 72, 61, 64, 70, 64, 62, 66, 68, 73, 70, 66, 63, 61, 62, 72, 64, 74, 75, 72, 65, 71, 65, 58, 70, 74, 69, 74 };

		int[] breathRate = new int[] { 12, 14, 14, 14, 14, 14, 14, 15, 14, 15, 15, 15, 14, 14, 14, 13, 13, 13, 14, 16, 13, 15, 15, 15, 14, 12, 14, 12, 12, 14,
				14, 13, 11, 14, 13, 13, 14, 12, 11, 12, 13, 12, 12, 16, 16, 15, 14, 15, 14, 14, 15, 15, 15, 14, 14, 14, 14, 14, 15, 14, 15, 15, 15, 14, 15, 16,
				15, 14, 15, 14, 15, 15, 15, 15, 13, 16, 13, 13, 12, 13, 13, 13, 12, 13, 12, 13, 14, 13, 13, 13, 14, 13, 12, 13, 12, 13, 13, 14, 15, 13, 15, 15,
				16, 14, 19, 11, 12, 13, 12, 12, 13, 16, 17, 15, 14, 14, 16, 15, 15, 14, 13, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 15, 14, 13, 15, 14, 13,
				13, 13, 13, 13, 13, 14, 14, 15, 15, 13, 14, 13, 16, 16, 16, 14, 15, 15, 12, 14, 16, 15, 13, 18, 20, 20, 18, 16 };

		int[] status = new int[] { 72, 72, 72, 12, 8, 12, 78, 72, 72, 72, 78, 78, 72, 72, 72, 78, 72, 8, 8, 14, 8, 8, 8, 14, 8, 14, 8, 8, 8, 14, 14, 12, 78, 76,
				72, 72, 76, 76, 78, 72, 8, 12, 78, 72, 78, 72, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 46,
				110, 104, 72, 78, 78, 72, 76, 72, 72, 72, 72, 8, 8, 8, 8, 8, 76, 72, 72, 72, 104, 104, 104, 104, 104, 104, 72, 72, 72, 78, 78, 78, 72, 72, 72,
				72, 78, 76, 78, 78, 12, 12, 12, 8, 14, 14, 12, 12, 12, 12, 12, 12, 14, 14, 12, 14, 14, 14, 12, 12, 14, 12, 12, 12, 12, 8, 12, 8, 12, 12, 12, 8,
				14, 12, 8, 14, 8, 12, 12, 14, 14, 12, 14, 72, 76, 78, 76, 76, 12, 14, 12, 8, 12 };
		int[] statusValue = new int[] { 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 1, 3, 1, 1, 0, 0, 1, 1, 2, 0,
				0, 3, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 2, 0, 0, 0, 0, 1, 4, 1, 1, 5, 3, 4, 0, 1, 3, 3, 3, 5, 5, 5, 3, 1, 1, 8, 1, 1, 2, 2, 1, 1, 4, 2,
				1, 1, 0, 1, 0, 3, 3, 1, 0, 1, 6, 0, 1, 0, 7, 8, 3, 1, 3, 1, 0, 1, 1, 5, 1, 1, 2, 2, 0, 1 };

		detail.setHeartRate(heartRate);
		detail.setBreathRate(breathRate);
		detail.setStatus(status);
		detail.setStatusValue(statusValue);
		historyData.setDetail(detail);

		Analysis analysis = AnalysisUtil.analysData(summ, detail, 1, DeviceType.DEVICE_TYPE_Z2);
		analysis.setAlgorithmVer(null);
		historyData.setAnalysis(analysis);
		return historyData;
	}

	
	
	private HistoryData createLongReportData(int starttime, int count) {
		HistoryData historyData = new HistoryData();
		Summary summ = new Summary();
		summ.setStartTime(starttime);
		summ.setRecordCount(count);
		historyData.setSummary(summ);

		Detail detail = new Detail();
		int[] heartRate = new int[] {57,65,68,74,77,70,60,60,64,63,59,62,57,66,61,65,52,57,61,67,65,64,59,60,56,61,62,
				54,62,57,58,67,63,55,61,58,66,62,63,66,63,63,64,63,64,59,54,60,63,60,55,64,58,65,69,78,76,73,73,76,71,73,75,73,73,73,74,74,71,72,70,70,72,72,72,71,69,69,68,69,69,66,64,66,
				68,67,68,68,68,67,66,67,67,66,66,66,68,67,67,65,67,67,67,66,67,65,66,66,66,65,65,65,65,67,67,65,65,66,66,66,65,66,66,64,65,65,65,65,65,65,66,65,65,65,64,67,69,72,75,69,70,
				68,68,70,65,62,68,64,69,68,65,62,67,66,66,67,64,66,66,64,64,65,64,66,65,65,66,63,63,63,63,63,67,66,62,64,65,65,64,63,69,64,61,65,65,64,64,64,64,63,63,63,66,64,63,64,65,63,
				64,64,64,64,64,67,64,65,65,63,63,62,63,61,59,64,63,64,63,58,60,62,62,65,64,68,64,64,68,65,65,66,65,65,64,64,64,66,63,65,64,65,65,66,65,63,64,62,63,63,63,63,62,62,59,61,57,
				62,62,60,58,61,60,62,63,62,58,63,60,63,61,63,60,61,62,63,62,61,63,62,59,61,61,59,64,63,63,62,60,57,65,63,61,66,64,64,63,66,61,57,63,59,64,60,61,61,63,57,55,58,60,60,62,60,
				59,61,58,68,62,67,63,66,63,64,64,57,65,64,57,58,61,62,61,64,60,61,61,64,63,63,63,62,62,60,65,60,61,64,65,62,62,63,61,65,63,60,61,61,61,63,61,61,61,63,61,64,62,63,62,64,62,
				62,64,61,62,61,62,64,62,64,63,62,58,62,61,62,63,61,60,58,58,61,59,62,60,60,63,66,63,63,58,60,61,63,63,61,63,61,62,66,60,63,62,63,61,61,64,63,62,63,62,61,62,59,61,63,60,58,
				61,63,61,61,60,66,62,62,61,62,64,62,62,62,60,60,63,62,63,62,62,60,60,62,61,60,60,61,62,63,62,62,63,64,63,59,63,63,61,63,64,64,60,63,61,61,63,63,63,64,62,62,63,63,63,62,63,
				64,64,65,65,64,63,65,64,64,62,63,64,65,63,64,63,62,64,68,64,64,64,60,62,61,61,65,61,65,65,61,61,71,70,68,67,65,65,63,62,61,62,64,61,65,67,62,66,66,62,62,60,60,60,63,65,60,
				58,58,62,60,54,57,65,62,65,66,65,61,60,59,62,62,62,62,63,58,62,62,60,59,62,65,63,60,61,63,60,62,62,62,61,63,64,64,65,63,64,64,63,65,66,65,64,65,64,65,65,64,66,65,64,71,71,
				69,68,66,67,67,62,66,67,64,66,65,62,67,67,71,58,57,55,0,0,0,0,0,0,0,55,62,64,61,64,64,67,65,65,72,67,69,68,69,69,69,69,69,65,66,64,66,66,66,67,66,65,68,65,57,65,61,68,66,
				64,61,60,57,58,66,66,57,59,65,67,65,65,58,72,59,59,61,68,66,61,64,72,68,66,64,60,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,65,65,72};

		int[] breathRate = new int[] {15,19,14,15,15,18,14,16,14,16,15,15,15,14,15,15,15,15,16,13,15,13,14,15,12,17,15,15,19,20,19,20,17,15,15,17,17,17,18,15,15,14,15,21,19,21,16,20,18,17,17,17,13,13,14,15,
				15,15,15,16,17,18,18,18,18,17,18,19,16,19,16,19,19,18,19,19,19,18,18,19,19,18,17,16,15,16,15,15,16,15,16,16,16,14,16,16,15,16,15,15,17,15,15,15,17,17,16,17,15,15,17,15,
				18,18,14,14,14,14,15,15,15,14,15,15,15,15,14,14,15,15,15,15,15,15,15,15,19,15,17,16,19,18,19,14,15,16,20,16,20,19,18,18,18,14,17,17,19,17,17,19,14,16,14,14,15,18,16,15,
				15,15,15,15,16,18,19,19,19,16,18,19,14,16,17,15,15,18,18,18,16,18,17,17,19,16,19,18,20,14,19,18,18,18,19,18,17,18,18,17,18,18,19,18,18,17,17,18,19,18,17,17,16,17,15,15,
				19,17,18,18,19,19,18,18,18,18,18,18,17,17,16,12,18,18,19,18,19,19,18,19,19,19,18,18,16,14,15,15,16,15,13,15,15,14,14,15,14,14,14,14,14,15,17,15,15,16,16,16,16,16,16,14,
				17,16,18,14,17,17,15,16,17,17,17,19,17,16,20,20,18,19,18,17,18,18,18,16,18,18,19,17,12,22,18,16,16,18,19,15,19,15,17,18,17,18,17,16,17,16,17,15,15,18,17,17,17,17,19,17,
				17,18,19,18,18,18,18,18,19,18,17,18,17,18,18,17,16,18,18,18,18,18,17,17,18,18,18,17,18,17,17,18,17,18,17,17,17,17,17,18,18,18,16,15,15,15,14,16,16,15,14,17,13,15,15,17,
				17,16,18,17,17,18,18,18,18,16,16,17,18,17,18,18,18,15,14,14,14,13,14,15,17,18,13,16,17,16,16,14,13,15,15,14,14,16,16,17,18,16,18,16,16,17,16,18,17,14,15,15,17,17,17,16,
				18,15,16,17,18,16,17,17,17,17,18,17,17,18,18,18,17,18,18,17,15,14,15,15,15,16,15,16,17,15,14,15,15,15,15,15,15,15,15,14,14,15,14,15,16,15,15,15,15,15,16,17,16,19,16,19,
				18,18,20,19,17,19,18,18,19,18,20,15,22,18,18,15,19,19,18,17,19,20,15,21,20,19,20,18,20,15,17,17,17,20,17,18,18,16,21,16,18,15,17,16,18,15,17,17,15,15,18,18,17,18,16,18,
				18,17,15,16,17,17,18,18,19,19,18,18,19,18,18,19,19,19,18,19,19,18,18,18,18,19,19,19,19,18,19,18,18,19,18,18,18,16,15,17,16,18,19,18,13,13,14,18,17,16,17,19,16,15,12,0,0,
				0,0,0,0,0,12,13,0,14,15,14,15,16,15,19,15,15,15,17,19,19,19,15,15,15,15,15,15,15,18,15,15,15,15,15,0,20,16,14,17,15,16,18,18,18,15,19,19,15,15,19,16,16,15,17,15,16,15,15,
				19,15,18,19,17,15,15,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,15,15,15};

		int[] status = new int[] {14,14,14,14,8,14,12,14,8,12,8,14,12,14,8,8,8,8,12,12,12,12,14,8,14,12,14,8,8,8,8,12,14,8,8,8,12,12,14,12,8,12,12,12,12,12,8,8,14,12,8,14,12,14,8,8,8,8,8,8,
				12,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,44,40,40,8,8,8,8,8,8,8,8,8,8,8,8,8,8,40,40,40,40,40,40,40,40,40,40,40,40,40,40,46,40,40,40,40,40,40,40,40,40,40,
				8,8,8,8,8,8,40,40,40,40,40,46,44,40,40,12,8,8,8,14,8,8,8,8,8,8,8,8,14,8,8,8,8,8,8,14,14,8,12,8,8,8,14,8,8,8,8,8,14,8,14,8,8,8,14,8,14,8,8,8,8,8,8,8,12,8,8,8,
				12,8,8,8,14,44,40,8,8,8,8,14,14,8,8,14,8,8,14,8,8,8,8,8,14,8,14,8,14,8,12,14,8,8,8,8,8,8,8,8,8,8,8,8,14,14,14,8,8,8,8,8,8,8,8,8,8,8,8,14,8,8,8,40,40,44,46,8,
				14,8,8,8,12,8,8,8,8,12,8,8,14,8,8,8,8,8,40,40,40,40,40,8,8,12,14,8,8,44,40,40,8,12,12,12,8,12,14,8,14,8,8,8,8,8,8,8,8,14,8,12,8,8,8,14,8,8,8,8,8,8,8,14,8,14,
				8,8,8,8,8,8,8,8,8,14,8,8,8,8,8,8,8,8,8,14,8,8,8,8,8,14,40,40,8,40,8,8,8,8,8,8,8,8,8,8,8,8,40,40,40,40,40,40,40,40,40,46,40,46,14,8,14,8,14,14,12,8,8,12,8,8,
				8,8,8,8,8,12,8,8,14,12,8,8,12,12,8,8,14,8,8,14,8,8,12,8,8,8,14,8,8,12,14,14,12,14,8,14,8,8,14,8,8,8,8,14,12,8,8,8,14,8,14,8,8,14,8,8,14,8,8,8,8,14,8,8,8,8,8,
				14,8,8,8,8,8,8,8,14,8,8,8,8,8,8,8,8,40,40,8,8,8,8,8,8,40,40,40,40,40,40,40,40,40,40,44,40,40,40,40,46,40,14,8,12,8,8,8,8,8,8,8,12,14,8,8,8,8,8,40,46,8,14,8,8,
				8,12,8,12,14,8,8,12,12,14,12,8,8,14,14,8,12,8,8,8,12,8,14,8,12,14,8,12,14,14,8,8,8,8,8,14,8,8,8,8,8,14,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,40,40,40,40,40,40,40,40,
				40,40,40,40,40,46,40,46,40,40,8,14,14,14,8,14,14,14,12,12,8,14,14,8,14,13,13,13,13,13,13,13,12,8,10,12,8,8,8,8,8,14,12,8,14,14,14,14,14,14,12,8,8,8,8,8,12,8,12,
				8,8,8,10,14,14,14,8,12,8,14,12,12,12,12,8,12,8,12,12,14,12,14,12,12,8,14,14,14,12,14,8,8,8,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,8,8,8};

		int[] statusValue = new int[] {1,2,1,1,0,1,2,1,0,1,0,1,1,1,0,0,0,0,2,2,1,2,1,0,1,1,1,0,0,0,0,2,2,0,0,0,1,1,1,2,0,1,1,1,1,1,0,0,1,1,0,1,2,2,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,2,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,1,
				0,2,0,0,0,2,0,0,0,0,0,1,0,1,0,0,0,1,0,1,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,2,1,0,0,0,0,0,1,1,0,0,1,0,0,1,0,0,0,0,0,1,0,1,0,1,0,2,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,
				0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,2,0,1,0,0,0,1,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,2,0,0,0,1,1,1,0,2,1,0,1,0,0,0,0,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0,0,0,0,1,
				0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,1,1,0,1,0,1,1,2,0,0,2,0,0,0,0,0,0,0,1,0,0,1,1,0,0,1,1,0,
				0,1,0,0,1,0,0,2,0,0,0,1,0,0,2,1,2,1,1,0,3,0,0,1,0,0,0,0,1,1,0,0,0,1,0,1,0,0,1,0,0,1,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,1,0,0,0,0,1,0,1,0,1,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,0,1,0,0,0,2,0,1,1,0,0,1,1,1,1,0,0,1,1,0,1,0,0,0,1,0,1,0,2,1,0,1,1,1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,3,1,1,0,1,1,1,1,1,0,2,1,0,1,57,60,60,60,60,60,60,2,0,12,2,0,0,0,0,0,1,1,0,1,1,2,1,2,1,1,0,0,0,0,0,1,0,2,0,0,0,
				11,1,1,1,0,1,0,1,1,3,1,1,0,2,0,1,1,1,2,1,1,2,0,1,2,1,1,1,0,0,0,12,60,60,60,60,60,60,60,60,60,42,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,9,18,0,0,0};

		detail.setHeartRate(heartRate);
		detail.setBreathRate(breathRate);
		detail.setStatus(status);
		detail.setStatusValue(statusValue);
		historyData.setDetail(detail);
		
//		Analysis analysis = AnalysisUtil.analysData(summ, detail, 0, DeviceType.DEVICE_TYPE_Z2);
//		SdkLog.log(TAG+" createLongReportData analysis:" + analysis.getAlgorithmVer());
		
		Analysis analysis = new Analysis();
		analysis.setMd_breath_low_decrease_scale((short)1);
		analysis.setMd_body_move_decrease_scale((short)4);
		analysis.setMd_leave_bed_decrease_scale((short)2);
		analysis.setMd_sleep_time_increase_scale((short)7);
		analysis.setMd_fall_asleep_time_decrease_scale((short)15);
		analysis.setMd_breath_stop_decrease_scale((short)2);
		analysis.setMd_start_time_decrease_scale((short)1);
		analysis.setMd_wake_cnt_decrease_scale((short)20);
		analysis.setMd_perc_effective_sleep_decrease_scale((short)7);
		
		analysis.setDuration(584);
		analysis.setSleepScore(41);
		analysis.setLightSleepAllTime(193);
		analysis.setInSleepAllTime(285);
		analysis.setDeepSleepAllTime(72);
		analysis.setDeepSleepPerc(10);
		analysis.setInSleepPerc(39);
		analysis.setLightSleepPerc(27);
		analysis.setWakeSleepPerc(24);
		analysis.setWake(62);
		analysis.setWakeTimes(6);
		analysis.setBreathPauseTimes(1);
		analysis.setBreathPauseAllTime(12);
		analysis.setHeartBeatPauseTimes(0);
		analysis.setHeartBeatPauseAllTime(0);
		analysis.setLeaveBedTimes(1);
		analysis.setOutOfBedDuration(417);
		analysis.setBodyMovementTimes(65);
		analysis.setTrunOverTimes(105);
		analysis.setAvgHeartRate(64);
		analysis.setAvgBreathRate(17);
		analysis.setMaxHeartBeatRate(75);
		analysis.setMaxBreathRate(22);
		analysis.setMinHeartBeatRate(54);
		analysis.setMinBreathRate(12);
		analysis.setBreathRateSlowAllTime(1);
		analysis.setFallAlseepAllTime(68);
		
		int fallsleepTimeStamp = summ.getStartTime() + analysis.getFallAlseepAllTime() * 60;
		int wakeupTimeStamp = fallsleepTimeStamp + analysis.getDuration() * 60;
		
		analysis.setFallsleepTimeStamp(fallsleepTimeStamp);
		analysis.setWakeupTimeStamp(wakeupTimeStamp);
		
//		analysis.setHeartBeatRateSlowAllTime(14);
//		analysis.setWakeAndLeaveBedBeforeAllTime(6);
//		analysis.setBreathRateFastAllTime(0);
		
		analysis.setSleepCurveArray(new float[]{0.0f,-0.023902f,-0.048756f,-0.009266f,-0.030327f,-0.022877f,-0.009779f,-0.01524f,-0.015893f,-0.030261f,-0.023268f,-0.018548f,-0.046933f,-0.045622f,-0.048341f,-0.028757f,-0.021429f,-2.75E-4f,-0.001263f,-0.029837f,-0.006594f,-0.04686f,-0.015524f,-0.041563f,-0.001669f,-0.042988f,-0.045256f,-0.009457f,-0.025738f,-0.013479f,-0.007247f,-0.014944f,-0.042886f,-0.042883f,-0.00696f,-0.014681f,-0.043486f,-0.026883f,-0.009825f,-0.042183f,-0.016081f,-0.044254f,-0.027137f,-0.023638f,-0.004208f,-0.005238f,-0.042136f,-0.044353f,-0.02431f,-0.024211f,-0.002863f,-0.00304f,-0.009653f,-0.017808f,-0.046687f,-0.028913f,-0.002786f,-0.012646f,-0.037875f,-0.012292f,-0.049331f,-0.017737f,-0.045937f,-0.047992f,-0.047585f,-0.030187f,-0.01214f,0.0f,0.830065f,0.965841f,1.108821f,1.259004f,1.416505f,1.581324f,1.74808f,1.916546f,2.006752f,2.091691f,2.171479f,2.245773f,2.314003f,2.376055f,2.431815f,2.481283f,2.52446f,2.561116f,2.591139f,2.614415f,2.63117f,2.646558f,2.660349f,2.672774f,2.68383f,2.693632f,2.702523f,2.710616f,2.718025f,2.72475f,2.730791f,2.736148f,2.741049f,2.745608f,2.564832f,2.56848f,2.571785f,2.575205f,2.578738f,2.582386f,2.586147f,2.589794f,2.593328f,2.596747f,2.600167f,2.603586f,2.607006f,2.610425f,2.613617f,2.80112f,2.802947f,2.803185f,2.800925f,2.796166f,2.78891f,2.779155f,2.765995f,2.750337f,2.732181f,2.711527f,2.688375f,2.662724f,2.634576f,2.604158f,2.571924f,2.536966f,2.500191f,2.462509f,2.423919f,2.384421f,2.344015f,2.303608f,2.261386f,2.217348f,2.171948f,2.124278f,2.074337f,2.023035f,1.970371f,1.915436f,1.859594f,1.803752f,1.747002f,1.690252f,1.633501f,1.575843f,1.517277f,1.45871f,1.400144f,1.341124f,1.283466f,1.226261f,1.168603f,1.109582f,1.050108f,0.989726f,0.928435f,0.868053f,0.808578f,0.750012f,0.694169f,0.640597f,0.589295f,0.54117f,0.49804f,0.458088f,0.42313f,0.393166f,0.368196f,0.346404f,0.330059f,0.318255f,0.310083f,0.304635f,0.301911f,0.301003f,0.303273f,0.307813f,0.315531f,0.324611f,0.335053f,0.345042f,0.355484f,0.366379f,0.380454f,0.395436f,0.41178f,0.429486f,0.447646f,0.464444f,0.480334f,0.492592f,0.50258f,0.507574f,0.50939f,0.506665f,0.499856f,0.488052f,0.472161f,0.451277f,0.427215f,0.399975f,0.370465f,0.338685f,0.30645f,0.274216f,0.243798f,0.214742f,0.189772f,0.165256f,0.142556f,0.122125f,0.103966f,0.088075f,0.076522f,0.067489f,0.061931f,0.058237f,0.058723f,0.063388f,0.074144f,0.091695f,0.117449f,0.149591f,0.189937f,0.236576f,0.290214f,0.352259f,0.422711f,0.499504f,0.581933f,0.665771f,0.750313f,0.831333f,0.910239f,0.985623f,1.05678f,1.121596f,1.180071f,1.230796f,1.277999f,1.322384f,1.363246f,1.400586f,1.434403f,1.462584f,1.487947f,1.510492f,1.533036f,1.554172f,1.57249f,1.58658f,1.595739f,1.599966f,1.600671f,1.598557f,1.595035f,1.589399f,1.582353f,1.57249f,1.562627f,1.553468f,1.548536f,1.547832f,1.552764f,1.561922f,1.574604f,1.590808f,1.610534f,1.633783f,1.658442f,1.679043f,1.695588f,1.706047f,1.71183f,1.712938f,1.711312f,1.704839f,1.693521f,1.675946f,1.650878f,1.620429f,1.58336f,1.540996f,1.49466f,1.444353f,1.391397f,1.333147f,1.268276f,1.195463f,1.117354f,1.032626f,0.942602f,0.849931f,0.754611f,0.66194f,0.571916f,0.488511f,0.410403f,0.336927f,0.268085f,0.2052f,0.152243f,0.111863f,0.086706f,0.074124f,0.072795f,0.082717f,0.099918f,0.125062f,0.1555f,0.191232f,0.230934f,0.277254f,0.328866f,0.385112f,0.445988f,0.510174f,0.575021f,0.640529f,0.7067f,0.774855f,0.843011f,0.913152f,0.98263f,1.051448f,1.11828f,1.183127f,1.243342f,1.299586f,1.352522f,1.40215f,1.44847f,1.495451f,1.54177f,1.587427f,1.6311f,1.673449f,1.712489f,1.75153f,1.789909f,1.828949f,1.868651f,1.910339f,1.952688f,1.995698f,2.039371f,2.083705f,2.127378f,2.173035f,2.218031f,2.262365f,2.303391f,2.341987f,2.376387f,2.407252f,2.434583f,2.457935f,2.47554f,2.488279f,2.496151f,2.499157f,2.496851f,2.489235f,2.473218f,2.451446f,2.423919f,2.393284f,2.359985f,2.324467f,2.286506f,2.246104f,2.203703f,2.159305f,2.112687f,2.064737f,2.015455f,1.965507f,1.915115f,1.864723f,1.814331f,1.764383f,1.715323f,1.668039f,1.622975f,1.580353f,1.540616f,1.503766f,1.470689f,1.442496f,1.418743f,1.398986f,1.383003f,1.371015f,1.362579f,1.357474f,1.354366f,1.35259f,1.351258f,1.350148f,1.347928f,1.344376f,1.339715f,1.333277f,1.325063f,1.314852f,1.302642f,1.288435f,1.271119f,1.25114f,1.228719f,1.2043f,1.178993f,1.15191f,1.122385f,1.091306f,1.058896f,1.024931f,0.989191f,0.951674f,0.912604f,0.872868f,0.833131f,0.794505f,0.756989f,0.722357f,0.690833f,0.662414f,0.638654f,0.619996f,0.606664f,0.599542f,0.598189f,0.603268f,0.614779f,0.63228f,0.655324f,0.682804f,0.714718f,0.751511f,0.791184f,0.833961f,0.878953f,0.925498f,0.974037f,1.024792f,1.077321f,1.131623f,1.187477f,1.244881f,1.303394f,1.363016f,1.423745f,1.484918f,1.545648f,1.606155f,1.665112f,1.723403f,1.78103f,1.837105f,1.891629f,1.943714f,1.993805f,2.042344f,2.088445f,2.132773f,2.175328f,2.216332f,2.256006f,2.294349f,2.331363f,2.368377f,2.404062f,2.438416f,2.470997f,2.501805f,2.530618f,2.556993f,2.580487f,2.601543f,2.619939f,2.635676f,2.64824f,2.657632f,2.663928f,2.666394f,2.66503f,2.660278f,2.651406f,2.639298f,2.623957f,2.605603f,2.584311f,2.560083f,2.532185f,2.500615f,2.46464f,2.425728f,2.383513f,2.339095f,2.292842f,2.244386f,2.193727f,2.142335f,2.089841f,2.036613f,1.981917f,1.926119f,1.86922f,1.812321f,1.755422f,1.698523f,1.64089f,1.582523f,1.522687f,1.462117f,1.401915f,1.342446f,1.282977f,1.223876f,1.164774f,1.105673f,1.045837f,0.986369f,0.926533f,0.866698f,0.807596f,0.749229f,0.691229f,0.634697f,0.578899f,0.524203f,0.470241f,0.417013f,0.36452f,0.313861f,0.266874f,0.225038f,0.187253f,0.153885f,0.124188f,0.098162f,0.076946f,0.061273f,0.051182f,0.047039f,0.048501f,0.056329f,0.070144f,0.090323f,0.116525f,0.148408f,0.185985f,0.230015f,0.279737f,0.335912f,0.39892f,0.46838f,0.544293f,0.625519f,0.71168f,0.801636f,0.89387f,0.989899f,1.089345f,1.191447f,1.294688f,1.39755f,1.498514f,1.598718f,1.697025f,1.793054f,1.886047f,1.975624f,2.059508f,2.136559f,2.206647f,2.270269f,2.325763f,2.372866f,2.411186f,2.441089f,2.462941f,2.4775f,2.483865f,2.481599f,2.466249f,2.437371f,2.394273f,2.339937f,2.276052f,2.202595f,2.119121f,2.026076f,1.922568f,1.809266f,1.686838f,1.555951f,1.416161f,1.270137f,1.118771f,0.962954f,0.803129f,0.641078f,0.477693f,0.31542f,0.155818f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.02949f,0.104162f,0.189076f,0.244499f,0.244499f,0.189076f,0.104162f,0.02949f,0.280419f,0.33762f,0.396848f,0.457973f,0.520472f,0.584217f,0.647166f,0.708134f,0.766597f,0.821155f,0.867742f,0.902533f,0.924651f,0.883842f,0.825817f,0.749301f,0.674419f,0.601569f,0.530752f,0.461967f,0.0f,-0.017808f,-0.046687f,-0.028913f,-0.002786f,-0.012646f,-0.037875f,-0.012292f,-0.049331f,-0.017737f,-0.045937f,-0.047992f,-0.047585f,-0.030187f,-0.01214f,-0.009195f,-0.007962f,-0.037271f,-0.020552f,-0.011153f,-0.034629f,-0.022431f,-0.032209f,-0.040871f,-0.044832f,-0.044307f,-0.016203f,-0.010146f,-0.002713f,-0.042769f,-0.041062f,-0.00566f,-0.048194f,-0.040099f,-0.003945f,-0.035087f,-0.040006f,-0.015654f,-0.031301f,-0.005814f,-0.00412f,-0.010492f,-0.007693f,-0.043919f,-0.019726f,-0.03407f,-0.03278f,-0.014025f,0.0f,-0.023902f,-0.048756f,-0.009266f,-0.030327f,-0.022877f,-0.009779f,-0.01524f,-0.015893f,-0.030261f,-0.023268f,-0.018548f,-0.046933f,-0.045622f});
		analysis.setSleepCurveStatusArray(new short[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,4,4,4,4,0,8,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,0,0});
		int len = summ.getRecordCount();
		int[] breathRateStatusAry = new int[len];
		int[] heartRateStatusAry = new int[len];
		int[] leftBedStatusAry = new int[len];
		int[] turnOverStatusAry = new int[len];
		for(int i=0;i<len;i++){
			if ((analysis.getSleepCurveStatusArray()[i] & SleepConfig.NewBreathPause) == SleepConfig.NewBreathPause) { // 呼吸暂停点
				breathRateStatusAry[i] = detail.getStatusValue()[i];
			}else if ((analysis.getSleepCurveStatusArray()[i] & SleepConfig.NewHeartPause) == SleepConfig.NewHeartPause) { // 心跳暂停点
				heartRateStatusAry[i] = detail.getStatusValue()[i];
			}else if ((analysis.getSleepCurveStatusArray()[i] & SleepConfig.NewLeaveBed) == SleepConfig.NewLeaveBed) { // 离床
				leftBedStatusAry[i] = detail.getStatusValue()[i];
			}else if ((analysis.getSleepCurveStatusArray()[i] & SleepConfig.NewTurnOver) == SleepConfig.NewTurnOver) { // 翻身
				turnOverStatusAry[i] = detail.getStatusValue()[i];
			}
		}
		analysis.setBreathRateStatusAry(breathRateStatusAry);
		analysis.setHeartRateStatusAry(heartRateStatusAry);
		analysis.setLeftBedStatusAry(leftBedStatusAry);
		analysis.setTurnOverStatusAry(turnOverStatusAry);
		
		historyData.setAnalysis(analysis);
		return historyData;
	}

	private List<CvPoint> points = new ArrayList<CvPoint>();
	private List<LineGraphView.BedBean> bedBeans = new ArrayList<LineGraphView.BedBean>();
	private List<LineGraphView.BedBean> SleepInUP = new ArrayList<LineGraphView.BedBean>();
	/**
	 * 描述：呼吸暂停的集合
	 */
	private List<GraphView.GraphViewData> apneaPauseList = new ArrayList<GraphView.GraphViewData>();
	/**
	 * 描述：心跳暂停的集合
	 */
	private List<GraphView.GraphViewData> heartPauseList = new ArrayList<GraphView.GraphViewData>();

	/**
	 * 新的睡眠曲线中离床的起始点，单位是分钟
	 */
	private int leaveBedStart = 0;

	
	/**
	 * <h3>新版 算出 睡眠周期图的数据结构</h3>
	 * 
	 * @param analysis
	 * @param timeStep
	 * @return
	 */
	private GraphView.GraphViewData[] getNewSleepGraphData(Detail detail, Analysis analysis, int timeStep, DeviceType deviceType) {
		GraphView.GraphViewData[] mainData = new GraphView.GraphViewData[analysis.getSleepCurveArray().length + 1];
		// 是手机监测的新版
		for (int i = 0; i < analysis.getSleepCurveArray().length; i++) {
			// 清醒，潜睡，中睡，深睡 手机给的是 0,1,2,3； ron画图的列表是: 1,0,-1,-2
			mainData[i] = new GraphView.GraphViewData(i * timeStep, 1 - analysis.getSleepCurveArray()[i]);
		}

		mainData[analysis.getSleepCurveArray().length] = new GraphView.GraphViewData(analysis.getSleepCurveArray().length * timeStep, 1);
		SleepInUP.clear();
		heartPauseList.clear();
		apneaPauseList.clear();
		bedBeans.clear();
		if (analysis.getSleepCurveStatusArray() != null && analysis.getSleepCurveStatusArray().length > 0) {
			for (int i = 0; i < analysis.getSleepCurveStatusArray().length; i++) {

				if ((analysis.getSleepCurveStatusArray()[i] & SleepConfig.NewSleepInPoint) == SleepConfig.NewSleepInPoint) { // 入睡点
					LineGraphView.BedBean sleepIn = new LineGraphView.BedBean();
					sleepIn.setData(new GraphView.GraphViewData(i * timeStep, 0));
					sleepIn.setX(i * timeStep);
					sleepIn.setStatus(BedBean.SLEEPIN);
					sleepIn.setY(0);
					SleepInUP.add(sleepIn);
				}

				if ((analysis.getSleepCurveStatusArray()[i] & SleepConfig.NewWakeUpPoint) == SleepConfig.NewWakeUpPoint) { // 清醒点
					LineGraphView.BedBean waleUp = new LineGraphView.BedBean();
					waleUp.setData(new GraphView.GraphViewData(i * timeStep, 0));
					waleUp.setX(i * timeStep);
					waleUp.setStatus(BedBean.SLEEPUP);
					waleUp.setY(0);
					SleepInUP.add(waleUp);
				}
				// 纽扣没有呼吸暂停和心率暂停
				if (deviceType != DeviceType.DEVICE_TYPE_SLEEPDOT) {
					if (analysis.getHeartRateStatusAry()[i] > 0) { // 心跳暂停点
						GraphView.GraphViewData heartPause = new GraphView.GraphViewData(i * timeStep, mainData[i].valueY);
						heartPause.setApneaRate(detail.getBreathRate()[i]);
						heartPause.setHeartRate(detail.getHeartRate()[i]);
						heartPause.setStatus(3);
						heartPause.setStatusValue(analysis.getHeartRateStatusAry()[i]);
						heartPauseList.add(heartPause);
					}
					if (analysis.getBreathRateStatusAry()[i] > 0) { // 呼吸暂停点
						GraphView.GraphViewData breathPause = new GraphView.GraphViewData(i * timeStep, mainData[i].valueY);
						breathPause.setApneaRate(detail.getBreathRate()[i]);
						breathPause.setHeartRate(detail.getHeartRate()[i]);
						breathPause.setStatus(2);
						breathPause.setStatusValue(analysis.getBreathRateStatusAry()[i]);
						apneaPauseList.add(breathPause);
					}
				}
				
				if (analysis.getLeftBedStatusAry()[i] > 0) { // 离床点
					if (i > 0) {
						if (analysis.getLeftBedStatusAry()[i - 1] == 0) {
							LineGraphView.BedBean wakeUp = new LineGraphView.BedBean();
							wakeUp.setX(i * timeStep);
							wakeUp.setY((float) mainData[i].getY());
							wakeUp.setData(new GraphView.GraphViewData(i * timeStep, (float) mainData[i].getY()));
							wakeUp.setWake(true);
							bedBeans.add(wakeUp);
							leaveBedStart = i;
						}
					}

					if (i + 1 < analysis.getSleepCurveStatusArray().length) {
						if (analysis.getLeftBedStatusAry()[i + 1] == 0) {
							LineGraphView.BedBean wakeIn = new LineGraphView.BedBean();
							wakeIn.setX(i * timeStep);
							wakeIn.setY((float) mainData[i].getY());
							wakeIn.setData(new GraphView.GraphViewData(i * timeStep, (float) mainData[i].getY()));
							wakeIn.setWake(false);
							wakeIn.setStatusValue((i - leaveBedStart) * 60);
							bedBeans.add(wakeIn);
						}
					}
					// 如果本身就是最后一个离床点，判断前一个是否是离床点，如果有连续两个离床点则认为是离床
					// 0,0,0,0,0,0,4,4,0,0,0,0,4,4,4,4,4,0,0,0,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4不加这个的话对这种情况会少一个离床点
					if (i + 1 == analysis.getSleepCurveStatusArray().length) {
						if (analysis.getLeftBedStatusAry()[i - 1]  > 0) {
							LineGraphView.BedBean wakeIn = new LineGraphView.BedBean();
							wakeIn.setX(i * timeStep);
							wakeIn.setY((float) mainData[i].getY());
							wakeIn.setData(new GraphView.GraphViewData(i * timeStep, (float) mainData[i].getY()));
							wakeIn.setWake(false);
							wakeIn.setStatusValue((i - leaveBedStart) * 60);
							bedBeans.add(wakeIn);
						}
					}

				}
			}
		}
		return mainData;
	}

	/**
	 * <p>
	 * 分析detail
	 * </p>
	 * 
	 * @param analysis
	 * @param timeStep
	 */
	public GraphView.GraphViewData[] getSleepGraphData(Detail detail, Analysis analysis, int timeStep, DeviceType deviceType) {
		if (analysis == null || analysis.getSleepCurveArray() == null || analysis.getSleepCurveArray().length == 0
				|| analysis.getSleepCurveStatusArray() == null || analysis.getSleepCurveStatusArray().length == 0){
			return null;
		}
		return getNewSleepGraphData(detail, analysis, timeStep, deviceType);
	}

	/**
	 * <p>
	 * 由于datas是按照x轴为时间轴的， 保证第一个数 是小于 提供值x的值，就是最近的值
	 * </p>
	 */
	public static GraphView.GraphViewData findNear(GraphView.GraphViewData[] datas, int x) {
		if (datas == null) {
			return null;
		}
		if (datas.length == 0)
			return null;

		if (datas[0].getX() > x)
			return null;

		for (int i = 0; i < datas.length; i++) {
			if (datas[i].getX() >= x)
				return datas[i];
		}
		return null;
	}

	/**
	 * <p>
	 * 由于datas是按照x轴为时间轴的， 保证第一个数 是小于 提供值x的值，就是最近的值
	 * </p>
	 */
	public static GraphView.GraphViewData findNear(GraphView.GraphViewData[] datas, int x, List<GraphView.GraphViewData> dt) {
		if (datas == null) {
			return null;
		}
		if (datas.length == 0)
			return null;

		if (datas[0].getX() > x)
			return null;

		for (int i = 0; i < datas.length; i++) {
			if (datas[i].getX() >= x) {
				if (dt != null)
					for (GraphView.GraphViewData gv : dt) {
						if (gv.getX() == datas[i].getX()) {
							if (i + 1 < datas.length) {
								return datas[i + 1];
							}
						}
					}
				return datas[i];
			}
		}
		return null;
	}

}




















