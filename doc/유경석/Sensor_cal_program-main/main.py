# BS_SCP_1_0_0.py

import sys
import os
from PySide6.QtWidgets import QApplication, QMainWindow, QCheckBox, QPushButton, QLabel, QTextBrowser, QWidget, QComboBox, QMessageBox
from PySide6.QtGui import QPixmap
from PySide6.QtCore import Qt, QTimer
from serial_comm import SerialComm
from ui_controller import UIController
from ph_calibration import PHCalibrationModule
from ec_calibration import ECCalibrationModule
from cl_calibration import CLCalibrationModule

def resource_path(relative_path):
    """ Get absolute path to resource, works for dev and for PyInstaller """
    try:
        # PyInstaller creates a temp folder and stores path in _MEIPASS
        base_path = sys._MEIPASS
    except Exception:
        base_path = os.path.abspath(".")
    return os.path.join(base_path, relative_path)

class WindowClass(QMainWindow):
    def __init__(self):
        super().__init__()
        try:
            self.version = "(v1.0.0)"
            self.setWindowTitle(f"BLUESEN BS-SCP{self.version}")
            self.setFixedSize(800, 600)
            
            self.sensor_read_status = False
            debug_mode = False  # debug Mode Select
            
            # Initialize serial communication instance
            self.serial_comm = SerialComm()
            self.serial_comm.data_received.connect(self.handle_received_data)
            
            # Initialize UI controller
            self.ui_controller = UIController(self)
            
            # Set up GUI elements
            self.setup_ui(debug_mode)
            
        except Exception as e:
            self.show_error_message(f"Initialization Error: {e}")

    def setup_ui(self, debug_mode):
        """Setup the main UI elements."""
        try:
            central_widget = QWidget(self)
            central_widget.setStyleSheet("background-color: #E2F6FF;")
            self.setCentralWidget(central_widget)
            
            if debug_mode:
                # Debug ComboBox for Sensor Type Selection
                self.sensor_type_combo = QComboBox(self)
                self.sensor_type_combo.setGeometry(640, 180, 100, 30)
                self.sensor_type_combo.addItems(["Select Type", "TU", "pH", "CL", "EC"])
                self.sensor_type_combo.currentTextChanged.connect(self.debug_select_sensor_type)
                print("debug Mode")
            
            # Bluesen Logo and program label
            bluesen_logo = QLabel(central_widget)
            logo_path = resource_path("pic/Bluesen_logo.png")
            pixmap = QPixmap(logo_path)
            bluesen_logo.setPixmap(pixmap)
            bluesen_logo.setGeometry(10, 0, 150, 50)

            program_label = QLabel("Sensor Calibrator", central_widget)
            program_label.setGeometry(240, 20, 350, 50)
            program_label.setStyleSheet("font-size: 34px; font-weight: bold; font-family: Gothic;")
            
            version_label = QLabel(f"{self.version}", central_widget)
            version_label.setGeometry(550, 28, 150, 50)
            version_label.setStyleSheet("font-size: 15px; font-weight: bold; font-family: Gothic;")

            # Sensor Information Section
            sensor_type_label = QLabel("Sensor Type :", central_widget)
            sensor_type_label.setStyleSheet("font-size: 16px; font-weight: bold; color:blue; font-family: Gothic;")
            sensor_type_label.setGeometry(130, 130, 120, 30)
            
            self.sensor_type_value = QLabel("", central_widget)
            self.sensor_type_value.setStyleSheet("border: 1px solid gray; border-radius: 5px;font-size: 16px; font-weight: bold;")
            self.sensor_type_value.setAlignment(Qt.AlignCenter)
            self.sensor_type_value.setGeometry(250, 130, 60, 30)
            
            sensor_value_label = QLabel("Sensor Value :", central_widget)
            sensor_value_label.setStyleSheet("font-size: 16px; font-weight: bold; color:blue; font-family: Gothic;")
            sensor_value_label.setGeometry(360, 130, 120, 30)
            
            self.sensor_value_display = QLabel("0.00", central_widget)
            self.sensor_value_display.setStyleSheet("border: 1px solid gray; border-radius: 5px;font-size: 16px; font-weight: bold;")
            self.sensor_value_display.setAlignment(Qt.AlignCenter)
            self.sensor_value_display.setGeometry(490, 130, 150, 30)
            
            self.sensor_read_button = QPushButton("READ", central_widget)
            self.sensor_read_button.setStyleSheet("font-size: 14px; font-weight: bold;")
            self.sensor_read_button.setGeometry(650, 125, 100, 40)
            self.sensor_read_button.setEnabled(False)
            self.sensor_read_button.clicked.connect(self.sensor_read_value)
            
            # Sensor Calibration Section
            sensor_select_label = QLabel("Calibration", central_widget)
            sensor_select_label.setGeometry(50, 180, 140, 50)
            sensor_select_label.setStyleSheet("font-size: 20px; color: darkblue;font-weight: bold;")
            
            # TU Calibration
            self.tu_checkbox = QCheckBox(" TU", self)
            self.tu_checkbox.setStyleSheet("font-size: 16px; font-weight: bold;")
            self.tu_checkbox.setGeometry(60, 235, 100, 30)
            self.tu_checkbox.stateChanged.connect(self.on_checkbox_changed)
            
            self.tu_cal_zero = QPushButton("Zero Calibration", central_widget)
            self.tu_cal_zero.setStyleSheet("font-size: 12px; font-weight: bold;")
            self.tu_cal_zero.setGeometry(135, 230, 130, 50)
            self.tu_cal_zero.clicked.connect(self.tu_zero_calibration)
            self.tu_cal_zero.setEnabled(False)
            
            self.tu_cal_sample = QPushButton("1 Point Sample", central_widget)
            self.tu_cal_sample.setStyleSheet("font-size: 12px; font-weight: bold;")
            self.tu_cal_sample.setGeometry(270, 230, 130, 50)
            self.tu_cal_sample.clicked.connect(self.tu_sample_calibration)
            self.tu_cal_sample.setEnabled(False)

            # pH Calibration
            self.ph_checkbox = QCheckBox(" pH", self)
            self.ph_checkbox.setStyleSheet("font-size: 16px; font-weight: bold;")
            self.ph_checkbox.setGeometry(60, 305, 100, 30)
            self.ph_checkbox.stateChanged.connect(self.on_checkbox_changed)
            
            self.ph_cal_buffer4 = QPushButton("2 Point Buffer 4", central_widget)
            self.ph_cal_buffer4.setStyleSheet("font-size: 12px; font-weight: bold;")
            self.ph_cal_buffer4.setGeometry(135, 300, 130, 50)
            self.ph_cal_buffer4.clicked.connect(self.ph_buffer4_calibration)
            self.ph_cal_buffer4.setEnabled(False)
            
            self.ph_cal_buffer7 = QPushButton("2 Point Buffer 7", central_widget)
            self.ph_cal_buffer7.setStyleSheet("font-size: 12px; font-weight: bold;")
            self.ph_cal_buffer7.setGeometry(270, 300, 130, 50)
            self.ph_cal_buffer7.clicked.connect(self.ph_buffer7_calibration)
            self.ph_cal_buffer7.setEnabled(False)

            # CL Calibration
            self.cl_checkbox = QCheckBox(" CL", central_widget)
            self.cl_checkbox.setStyleSheet("font-size: 16px; font-weight: bold;")
            self.cl_checkbox.setGeometry(60, 375, 100, 30)
            self.cl_checkbox.stateChanged.connect(self.on_checkbox_changed)
            
            self.cl_cal_zero = QPushButton("Zero Calibration", central_widget)
            self.cl_cal_zero.setStyleSheet("font-size: 12px; font-weight: bold;")
            self.cl_cal_zero.setGeometry(135, 370, 130, 50)
            self.cl_cal_zero.clicked.connect(self.cl_zero_calibration)
            self.cl_cal_zero.setEnabled(False)
            
            self.cl_cal_sample = QPushButton("1 Point Sample", central_widget)
            self.cl_cal_sample.setStyleSheet("font-size: 12px; font-weight: bold;")
            self.cl_cal_sample.setGeometry(270, 370, 130, 50)
            self.cl_cal_sample.clicked.connect(self.cl_sample_calibration)
            self.cl_cal_sample.setEnabled(False)

            # EC Calibration
            self.ec_checkbox = QCheckBox(" EC", central_widget)
            self.ec_checkbox.setStyleSheet("font-size: 16px; font-weight: bold;")
            self.ec_checkbox.setGeometry(60, 445, 100, 30)
            self.ec_checkbox.stateChanged.connect(self.on_checkbox_changed)
            
            self.ec_cal_zero = QPushButton("Zero Calibration", central_widget)
            self.ec_cal_zero.setStyleSheet("font-size: 12px; font-weight: bold;")
            self.ec_cal_zero.setGeometry(135, 440, 130, 50)
            self.ec_cal_zero.clicked.connect(self.ec_zero_calibration)
            self.ec_cal_zero.setEnabled(False)
            
            self.ec_cal_sample = QPushButton("1 Point Sample", central_widget)
            self.ec_cal_sample.setStyleSheet("font-size: 12px; font-weight: bold;")
            self.ec_cal_sample.setGeometry(270, 440, 130, 50)
            self.ec_cal_sample.clicked.connect(self.ec_sample_calibration)
            self.ec_cal_sample.setEnabled(False)

            # Status Section
            status_label = QLabel("Status", central_widget)
            status_label.setGeometry(420, 180, 140, 50)
            status_label.setStyleSheet("font-size: 20px; color: darkblue; font-weight: bold;")
            
            self.good_label = QLabel("Good", central_widget)
            self.good_label.setStyleSheet("font-size: 16px; font-weight: bold; border: 2px lightgreen; padding: 5px; border-radius: 15px; background-color: lightcoral;  color: white;")
            self.good_label.setAlignment(Qt.AlignCenter)
            self.good_label.setGeometry(425, 230, 100, 30)
            
            self.error_label = QLabel("Error", central_widget)
            self.error_label.setStyleSheet("font-size: 16px; font-weight: bold; border: 2px lightcoral; padding: 5px; border-radius: 15px; background-color: lightcoral; color: white;")
            self.error_label.setAlignment(Qt.AlignCenter)
            self.error_label.setGeometry(535, 230, 100, 30)
            
            self.finish_label = QLabel("Finish", central_widget)
            self.finish_label.setStyleSheet("font-size: 16px; font-weight: bold; border: 2px lightblue; padding: 5px; border-radius: 15px; background-color: lightcoral; color: white;")
            self.finish_label.setAlignment(Qt.AlignCenter)
            self.finish_label.setGeometry(645, 230, 100, 30)

            # TextBrowser Section
            self.text_browser = QTextBrowser(central_widget)
            self.text_browser.setGeometry(420, 280, 330, 210)
            # text_browser.setHtml(""" <div style="line-height: 1.5;">
            #                             After placing the turbidity sensor in distilled water, press "NEXT".
            #                         </div>          """)
            
            # Footer Buttons
            # self.next_button = QPushButton("NEXT", central_widget)
            # self.next_button.setStyleSheet("font-size: 14px; font-weight: bold;")
            # self.next_button.setGeometry(420, 440, 130, 50)
            # self.next_button.clicked.connect(self.next_button_action)
            # self.next_button.setEnabled(False)
            
            # self.reset_button = QPushButton("RESET", central_widget)
            # self.reset_button.setStyleSheet("font-size: 14px; font-weight: bold;")
            # self.reset_button.setGeometry(550, 440, 130, 50)
            # self.reset_button.clicked.connect(self.reset_button_action)
            # self.reset_button.setEnabled(False)

            # Serial Port Section
            serial_label = QLabel("Com Port", central_widget)
            serial_label.setGeometry(50, 500, 180, 50)
            serial_label.setStyleSheet("font-size: 18px; color: darkblue; font-weight: bold;")
            
            port_label = QLabel("Port:", central_widget)
            port_label.setStyleSheet("font-size: 12px; font-weight: bold; font-family: Gothic;")
            port_label.setGeometry(60, 550, 30, 30)
            
            self.port_select = QComboBox(central_widget)
            self.port_select.setGeometry(100, 550, 80, 30)
            self.port_select.addItems(self.serial_comm.available_ports())

            baud_label = QLabel("Baud Rate:", central_widget)
            baud_label.setStyleSheet("font-size: 12px; font-weight: bold; font-family: Gothic;")
            baud_label.setGeometry(200, 550, 80, 30)
            
            self.baud_select = QComboBox(central_widget)
            self.baud_select.setGeometry(280, 550, 120, 30)
            self.baud_select.addItems(["9600", "19200", "38400", "57600", "115200"])
            
            self.connect_button = QPushButton("Connect", central_widget)
            self.connect_button.setGeometry(420, 550, 100, 30)
            self.connect_button.clicked.connect(self.connect_serial)

            self.disconnect_button = QPushButton("Disconnect", central_widget)
            self.disconnect_button.setGeometry(530, 550, 100, 30)
            self.disconnect_button.clicked.connect(self.disconnect_serial)
            self.disconnect_button.setEnabled(False)
            
        except Exception as e:
            self.show_error_message(f"UI Setup Error: {e}")

    def connect_serial(self):
        try:
            port_name = self.port_select.currentText()
            baud_rate = int(self.baud_select.currentText())
            if self.serial_comm.connect_serial(port_name, baud_rate):
                self.ui_controller.reset_ui_state()
                
                # 체크박스 상태 업데이트
                self.ui_controller.update_checkboxes()
                
                # 0.5초 후 체크박스 비활성화
                #QTimer.singleShot(500, self.ui_controller.disable_selected_checkbox)
                
                # sensor_read_button 활성화
                self.sensor_read_button.setEnabled(True)
                
                self.connect_button.setEnabled(False)
                self.disconnect_button.setEnabled(True)
                self.sensor_type_read()
            else:
                self.show_error_message("Failed to connect to the serial port.")
        except Exception as e:
            self.show_error_message(f"Serial Connection Error: {e}")

    # def disconnect_serial(self):
    #     try:
    #         self.serial_comm.disconnect_serial()
    #         self.ui_controller.reset_ui_state()
    #     except Exception as e:
    #         self.show_error_message(f"연결 해제 중 오류 발생: {e}")

    def disconnect_serial(self):
        try:
            # 캘리브레이션 모듈 정리
            if hasattr(self, 'ph_cal_module'):
                delattr(self, 'ph_cal_module')
            elif hasattr(self, 'ec_cal_module'):
                delattr(self, 'ec_cal_module')
            elif hasattr(self, 'cl_cal_module'):
                delattr(self, 'cl_cal_module')
            
            # 시리얼 연결 해제
            self.serial_comm.disconnect_serial()
            
            # UI 초기화
            self.ui_controller.reset_ui_state()
            
            # 연결 버튼 활성화
            self.connect_button.setEnabled(True)
            self.disconnect_button.setEnabled(False)
            
            # 텍스트 브라우저 초기화
            self.text_browser.clear()
            self.text_browser.setHtml(""" 
                        <div style="line-height: 1.5;">
                            <span style="font-size: 18px; font-weight: bold;">Welcome to Sensor Calibration Program</span><br>
                            (Please connect the sensor first)
                        </div>          """)
            
            # 상태 변수 초기화
            self.sensor_read_status = False
            self.sensor_type_read_status = False
            
        except Exception as e:
            self.show_error_message(f"연결 해제 중 오류 발생: {e}")
    
    def handle_received_data(self, data):
        try:
            parts = data.split()
            print("parts : ", parts)   # debug
            # pH 캘리브레이션 모듈이 활성화된 경우, 해당 응답 처리를 위임
            if hasattr(self, 'ph_cal_module') and self.ph_cal_module.is_active():
                self.ph_cal_module.handle_response(data)
                #return
            
            # ec 캘리브레이션 모듈이 활성화된 경우, 해당 응답 처리를 위임
            elif hasattr(self, 'ec_cal_module') and self.ec_cal_module.is_active():
                self.ec_cal_module.handle_response(data)
                #return
                
            # cl 캘리브레이션 모듈이 활성화된 경우, 해당 응답 처리를 위임
            elif hasattr(self, 'cl_cal_module') and self.cl_cal_module.is_active():
                self.cl_cal_module.handle_response(data)
            
            print("sensor_read_status : ", self.sensor_read_status)   # debug
            # 센서값 읽기
            if self.sensor_read_status:
                if len(parts) >= 2:
                    self.sensor_value_display.setStyleSheet("border: 1px solid gray; border-radius: 5px;font-size: 16px; font-weight: bold; color : red")
                    last_value = parts[-1]
                    mVolt = parts[-2]
                    self.sensor_value_display.setText(f"{last_value}({mVolt}mV)")
                    
                if data == "ERROR":
                    self.sensor_value_display.setStyleSheet("border: 1px solid gray; border-radius: 5px;font-size: 16px; font-weight: bold; color : black")
                    self.sensor_value_display.setText("0.00")
                    self.sensor_read_status = False
                    print("ERROR")   # debug
            # 센서 타입 읽기
            if self.sensor_type_read_status:
                if parts[0] == "04":
                    self.sensor_type_value.setText("EC")
                    self.text_browser.setHtml(""" 
                            <div style="line-height: 1.5;">
                                <span style="font-size: 18px; font-weight: bold;">STEP 1</span><br>
                                (Expose the sensor electrodes to the air without touching anything, let the sensor 
                                dry completely, Press the "zero calibration" button (until the measurement value stabilizes).)
                            </div>          """)
                elif parts[0] == "01":
                    self.sensor_type_value.setText("pH")
                    self.text_browser.setHtml(""" 
                            <div style="line-height: 1.5;">
                                <span style="font-size: 18px; font-weight: bold;">STEP 1</span><br>
                                (Immerse the pH sensor in the conical tube containing pH buffer 4 and wait 
                                for stabilization for about 5 minutes., After 5 minutes, press the"2 point buffer 4 "button .)
                            </div>          """)
                elif parts[0] == "07":
                    self.sensor_type_value.setText("TU")
                elif parts[0] == "10":
                    self.sensor_type_value.setText("CL")
                    self.text_browser.setHtml(""" 
                            <div style="line-height: 1.5;">
                                <span style="font-size: 18px; font-weight: bold;">STEP 1</span><br>
                                (Immerse the residual chlorine sensor in ultrapure water and wait at least 30 minutes for the mV value to stabilize. 
                                After stabilization, press the "zero calibration" button.)
                            </div>          """)
                else:
                    print("No Sensor Type: ", parts)
                
                self.sensor_type_read_status = False
                self.ui_controller.update_checkboxes()
        except Exception as e:
            self.show_error_message(f"Data Handling Error: {e}")

    def sensor_read_value(self):
        try:
            if self.sensor_read_button.text() == "READ":
                command = "CSNSR 1\r"
                self.serial_comm.send_data(command)
                self.sensor_read_button.setText("STOP")
                self.ui_controller.enable_calibration_buttons(False)
                
                # self.ui_controller.update_status_background("good", "green")
                
            else:
                command = "ESCAPE\r"
                self.serial_comm.send_data(command)
                self.sensor_read_button.setText("READ")
                self.ui_controller.enable_calibration_buttons(True)
                
                # self.ui_controller.update_status_background("good", "red")
                
            self.sensor_read_status = True
        except Exception as e:
            self.show_error_message(f"Sensor Read Error: {e}")

    def sensor_type_read(self):
        try:
            command = "GSTYPE\r"
            self.serial_comm.send_data(command)
            self.sensor_type_read_status = True
        except Exception as e:
            self.show_error_message(f"Sensor Type Read Error: {e}")

    def debug_select_sensor_type(self, sensor_type):
        if sensor_type != "Select Type":
            self.sensor_type_value.setText(sensor_type)
            self.ui_controller.update_checkboxes()

    def on_checkbox_changed(self):
        self.ui_controller.on_checkbox_changed(self.sender())

    def tu_zero_calibration(self):
        if self.sensor_type_value.text() == "TU":
            print("TU Zero Calibration clicked")
        else:
            print("Type is not TU")
    
    def tu_sample_calibration(self):
        if self.sensor_type_value.text() == "TU":
            print("TU 1 Point Sample clicked")
        else:
            print("Type is not TU")
    
    def ph_buffer4_calibration(self):
        if self.sensor_type_value.text() == "pH":
            if not hasattr(self, 'ph_cal_module'):
                self.ph_cal_module = PHCalibrationModule(self, self.serial_comm)
            self.ph_cal_module.calibration_step1()
        else:
            print("Type is not pH")
            
    def ph_buffer7_calibration(self):
        if self.sensor_type_value.text() == "pH":
            if not hasattr(self, 'ph_cal_module'):
                self.ph_cal_module = PHCalibrationModule(self, self.serial_comm)
            self.ph_cal_module.calibration_step2()
            print("ph_buffer7_calibration")   # debug
        else:
            print("Type is not pH")
            
    def cl_zero_calibration(self):
        if self.sensor_type_value.text() == "CL":
            if not hasattr(self, 'cl_cal_module'):
                self.cl_cal_module = CLCalibrationModule(self, self.serial_comm)
            self.cl_cal_module.calibration_step1()
            print("cl_zero_calibration")   # debug
        else:
            print("Type is not CL")
            
    def cl_sample_calibration(self):
        if self.sensor_type_value.text() == "CL":
            if not hasattr(self, 'cl_cal_module'):
                self.cl_cal_module = CLCalibrationModule(self, self.serial_comm)
            self.cl_cal_module.calibration_step2()
        else:
            print("Type is not CL")
            
    def ec_zero_calibration(self):
        if self.sensor_type_value.text() == "EC":
            if not hasattr(self, 'ec_cal_module'):
                self.ec_cal_module = ECCalibrationModule(self, self.serial_comm)
            self.ec_cal_module.calibration_step1()
            print("ec_zero_calibration")   # debug
        else:
            print("Type is not EC")
            
    def ec_sample_calibration(self):
        if self.sensor_type_value.text() == "EC":
            if not hasattr(self, 'ec_cal_module'):
                self.ec_cal_module = ECCalibrationModule(self, self.serial_comm)
            self.ec_cal_module.calibration_step2()
        else:
            print("Type is not EC")
            
    # def next_button_action(self):
    #     self.ui_controller.update_status_background("good", "green")
    #     print("Next Button Clicked")
        
    # def reset_button_action(self):
    #     self.ui_controller.update_status_background("good", "red")
    #     print("Reset Button Clicked")

    def show_error_message(self, message):
        """Display an error message in a message box."""
        error_box = QMessageBox()
        error_box.setIcon(QMessageBox.Critical)
        error_box.setWindowTitle("Error")
        error_box.setText(message)
        error_box.exec()

if __name__ == "__main__":
    app = QApplication(sys.argv)
    icon_path = resource_path("pic/Bluesen.ico")
    app.setWindowIcon(QPixmap(icon_path))
    myWindow = WindowClass()
    myWindow.show()
    sys.exit(app.exec())
