from PySide6.QtCore import QTimer

class PHCalibrationModule:
    def __init__(self, main_window, serial_comm):
        try:
            self.main = main_window
            self.serial_comm = serial_comm
            self.ui_controller = main_window.ui_controller
            self.current_step = 1
            self.waiting_for_ok = False
            self.monitoring_active = False
            self._active = False
            
            # Timer for calibration status
            self.timer = QTimer()
            self.timer.timeout.connect(self.read_calibration_status)
        except Exception as e:
            self.main.show_error_message(f"pH Calibration Module 초기화 오류: {e}")

    def is_active(self):
        return self._active

    def handle_response(self, data):
        try:
            if self.waiting_for_ok:
                if data == "OK":
                    self.waiting_for_ok = False
                    if self.current_step == 1:
                        self.start_monitoring_step1()
                    elif self.current_step == 2:
                        self.start_monitoring_step2()
                return

            if self.monitoring_active:
                status = data.split()
                self.process_status(status)
        except Exception as e:
            self.main.show_error_message(f"응답 처리 오류: {e}")
            self.reset_to_step1()

    def calibration_step1(self):
        try:
            self.ui_controller.update_status_background("error", "red")    # error 상태 초기화
            self._active = True
            self.waiting_for_ok = True
            self.serial_comm.send_data("CALS2PB 0\r")
        except Exception as e:
            self.main.show_error_message(f"Calibration Step 1 오류: {e}")
            self._active = False
    
    def calibration_step2(self):
        try:
            if self.current_step == 2:
                self.ui_controller.update_status_background("good", "red")   # good 상태 초기화
                self._active = True
                self.waiting_for_ok = True
                self.serial_comm.send_data("CALS2PB 1\r")
            else:
                self.ui_controller.update_status_background("error", "green")   # error 상태
                self.main.text_browser.append("Please complete STEP 1 first")
                self.current_step = 1
        except Exception as e:
            self.main.show_error_message(f"Calibration Step 2 오류: {e}")
            self._active = False

    def start_monitoring_step1(self):
        try:
            self.ui_controller.enable_calibration_buttons(False)    # 버튼 비활성화
            self.main.text_browser.append("pH buffer 4 applied, starting monitoring...")
            self.monitoring_active = True
            self.timer.start(5000)
        except Exception as e:
            self.main.show_error_message(f"모니터링 시작 오류 (Step 1): {e}")
            self.reset_to_step1()

    def start_monitoring_step2(self):
        try:
            self.ui_controller.enable_calibration_buttons(False)    # 버튼 비활성화
            self.main.text_browser.append("pH buffer 7 applied, starting monitoring...")
            self.monitoring_active = True
            self.timer.start(5000)
        except Exception as e:
            self.main.show_error_message(f"모니터링 시작 오류 (Step 2): {e}")
            self.reset_to_step1()

    def read_calibration_status(self):
        try:
            if self.serial_comm.serial.isOpen():
                self.serial_comm.send_data("CALSTATUS\r")
            else:
                raise Exception("시리얼 포트가 열려있지 않습니다.")
        except Exception as e:
            self.main.show_error_message(f"캘리브레이션 상태 읽기 오류: {e}")
            self.timer.stop()
            self._active = False
    
    def process_status(self, status):
        try:
            self.main.sensor_read_button.setEnabled(False)
            print("status : ", status)
            if self.current_step == 1:
                if status == ['01', '02', '01']:
                    self.main.text_browser.append("Processing... waiting in 5 seconds")
                elif status[0] != '01' or status[1] != '02'or (status[2] != '01' and status[2] != '02'):
                    self.main.text_browser.append("Error in calibration STEP 1")
                    self.reset_to_step1()
                elif status == ['01', '02', '02']:
                    self.complete_step1()
            
            elif self.current_step == 2:
                if status == ['01', '03', '01']:
                    self.main.text_browser.append("Processing... waiting in 5 seconds")
                elif status[0] != '01' or status[1] != '03' or (status[2] != '01' and status[2] != '02'):
                    self.main.text_browser.append("Error in calibration STEP 2")
                    self.reset_to_step1()    
                elif status == ['01', '03', '02']:
                    self.complete_calibration()
        except Exception as e:
            self.main.show_error_message(f"상태 처리 오류: {e}")
            self.reset_to_step1()

    def complete_step1(self):
        try:
            self.main.sensor_read_button.setEnabled(True)
            self.ui_controller.enable_calibration_buttons(True)  # 버튼 활성화
            self.ui_controller.update_status_background("good", "green")    # good 상태로 업데이트
            self.main.text_browser.append("Calibration STEP 1 complete")
            self.main.text_browser.setHtml(""" 
                            <div style="line-height: 1.5;">
                                <span style="font-size: 18px; font-weight: bold;">STEP 2</span><br>
                                (Immerse the pH sensor in the conical tube containing pH buffer 7 and wait 
                                for stabilization for about 5 minutes., After 5 minutes, Press the "2 point buffer 7" button.)
                            </div>          """)
            self.monitoring_active = False
            self.current_step = 2  # STEP 2로 업데이트
            self.timer.stop()
        except Exception as e:
            self.main.show_error_message(f"Step 1 완료 처리 오류: {e}")
            self.reset_to_step1()

    def complete_calibration(self):
        try:
            self.main.sensor_read_button.setEnabled(True)
            self.ui_controller.update_status_background("finish", "green")
            self.ui_controller.update_status_background("good", "red")
            self.ui_controller.update_status_background("error", "red")
            self.main.text_browser.setHtml(""" 
                            <div style="line-height: 1.5;">
                                <span style="font-size: 18px; font-weight: bold;">Calibration Success..</span><br>
                                (Please Press the "Disconnect" button and Remove the sensor.)
                            </div>          """)
            
            
            self.monitoring_active = False
            self._active = False  # 캘리브레이션 완료 시 비활성화
            self.timer.stop()
        except Exception as e:
            self.main.show_error_message(f"캘리브레이션 완료 처리 오류: {e}")
            self.reset_to_step1()

    def reset_to_step1(self):
        try:
            self.main.sensor_read_button.setEnabled(True)
            self.ui_controller.enable_calibration_buttons(True)  # 버튼 활성화
            self.ui_controller.update_status_background("error", "green")
            self.current_step = 1
            self.monitoring_active = False
            self._active = False  # 리셋 시 비활성화
            self.timer.stop()
            self.main.text_browser.setHtml(""" 
                            <div style="line-height: 1.5;">
                                <span style="font-size: 18px; font-weight: bold;">STEP 1</span><span style="font-size: 16px; font-weight: bold; color: red;">(Retrying)</span><br>
                                (Immerse the pH sensor in the conical tube containing pH buffer 4 and wait 
                                for stabilization for about 5 minutes., After 5 minutes, press the "2 point buffer 4" button.)
                            </div>          """)
        except Exception as e:
            self.main.show_error_message(f"Step 1 리셋 오류: {e}")
