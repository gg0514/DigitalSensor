from PySide6.QtWidgets import QCheckBox, QMessageBox

class UIController:
    def __init__(self, main_window):
        self.main = main_window  # 메인 윈도우 참조 저장

    def reset_ui_state(self):
        """Reset all UI elements to their initial state"""
        try:
            # 센서 타입과 값 초기화
            self.main.sensor_type_value.setText("")
            self.main.sensor_value_display.setText("0.00")
            self.main.sensor_value_display.setStyleSheet("border: 1px solid gray; border-radius: 5px;font-size: 16px; font-weight: bold;")

            # 체크박스 초기화
            checkboxes = [
                self.main.tu_checkbox,
                self.main.ph_checkbox,
                self.main.cl_checkbox,
                self.main.ec_checkbox
            ]
            for checkbox in checkboxes:
                checkbox.setEnabled(False)
                checkbox.setChecked(False)

            # 캘리브레이션 버튼 초기화
            calibration_buttons = [
                self.main.tu_cal_zero, self.main.tu_cal_sample,
                self.main.ph_cal_buffer4, self.main.ph_cal_buffer7,
                self.main.cl_cal_zero, self.main.cl_cal_sample,
                self.main.ec_cal_zero, self.main.ec_cal_sample
            ]
            for button in calibration_buttons:
                button.setEnabled(False)

            # 상태 표시 레이블 초기화
            status_labels = [self.main.good_label, self.main.error_label, self.main.finish_label]
            for label in status_labels:
                label.setStyleSheet("font-size: 16px; font-weight: bold; border: 2px lightcoral; padding: 5px; border-radius: 15px; background-color: lightcoral; color: white;")

            # 기타 버튼 상태 초기화
            self.main.sensor_read_button.setEnabled(False)
            self.main.sensor_read_button.setText("READ")
            # self.main.next_button.setEnabled(False)
            # self.main.reset_button.setEnabled(False)

            # 연결 관련 버튼 상태 업데이트
            self.main.connect_button.setEnabled(True)
            self.main.disconnect_button.setEnabled(False)

        except Exception as e:
            self.show_error_message(f"UI 초기화 중 오류 발생: {e}")

    def on_checkbox_changed(self, sender):
        """Handle checkbox selection and enable relevant calibration buttons."""
        try:
            # Uncheck other checkboxes if one is selected
            if sender == self.main.tu_checkbox and self.main.tu_checkbox.isChecked():
                self.main.ph_checkbox.setChecked(False)
                self.main.cl_checkbox.setChecked(False)
                self.main.ec_checkbox.setChecked(False)
                self.update_calibration_buttons("TU")
            
            elif sender == self.main.ph_checkbox and self.main.ph_checkbox.isChecked():
                self.main.tu_checkbox.setChecked(False)
                self.main.cl_checkbox.setChecked(False)
                self.main.ec_checkbox.setChecked(False)
                self.update_calibration_buttons("pH")
            
            elif sender == self.main.cl_checkbox and self.main.cl_checkbox.isChecked():
                self.main.tu_checkbox.setChecked(False)
                self.main.ph_checkbox.setChecked(False)
                self.main.ec_checkbox.setChecked(False)
                self.update_calibration_buttons("CL")
            
            elif sender == self.main.ec_checkbox and self.main.ec_checkbox.isChecked():
                self.main.tu_checkbox.setChecked(False)
                self.main.ph_checkbox.setChecked(False)
                self.main.cl_checkbox.setChecked(False)
                self.update_calibration_buttons("EC")
                
        except Exception as e:
            self.show_error_message(f"체크박스 상태 변경 중 오류 발생: {e}")
            try:
                self.main.tu_checkbox.setChecked(False)
                self.main.ph_checkbox.setChecked(False)
                self.main.cl_checkbox.setChecked(False)
                self.main.ec_checkbox.setChecked(False)
                self.update_calibration_buttons("")
            except:
                pass

    def update_checkboxes(self):
        """Enable and check the checkbox for the current sensor type and update related buttons."""
        try:
            sensor_config = {
                "EC": {
                    "active_checkbox": self.main.ec_checkbox,
                    "inactive_checkboxes": [self.main.ph_checkbox, self.main.tu_checkbox, self.main.cl_checkbox],
                    "active_buttons": [self.main.ec_cal_sample, self.main.ec_cal_zero],
                    "inactive_buttons": [
                        self.main.ph_cal_buffer4, self.main.ph_cal_buffer7,
                        self.main.tu_cal_zero, self.main.tu_cal_sample,
                        self.main.cl_cal_zero, self.main.cl_cal_sample
                    ]
                },
                "pH": {
                    "active_checkbox": self.main.ph_checkbox,
                    "inactive_checkboxes": [self.main.ec_checkbox, self.main.tu_checkbox, self.main.cl_checkbox],
                    "active_buttons": [self.main.ph_cal_buffer4, self.main.ph_cal_buffer7],
                    "inactive_buttons": [
                        self.main.ec_cal_sample, self.main.ec_cal_zero,
                        self.main.tu_cal_zero, self.main.tu_cal_sample,
                        self.main.cl_cal_zero, self.main.cl_cal_sample
                    ]
                },
                "TU": {
                    "active_checkbox": self.main.tu_checkbox,
                    "inactive_checkboxes": [self.main.ec_checkbox, self.main.ph_checkbox, self.main.cl_checkbox],
                    "active_buttons": [self.main.tu_cal_zero, self.main.tu_cal_sample],
                    "inactive_buttons": [
                        self.main.ec_cal_sample, self.main.ec_cal_zero,
                        self.main.ph_cal_buffer4, self.main.ph_cal_buffer7,
                        self.main.cl_cal_zero, self.main.cl_cal_sample
                    ]
                },
                "CL": {
                    "active_checkbox": self.main.cl_checkbox,
                    "inactive_checkboxes": [self.main.ec_checkbox, self.main.ph_checkbox, self.main.tu_checkbox],
                    "active_buttons": [self.main.cl_cal_zero, self.main.cl_cal_sample],
                    "inactive_buttons": [
                        self.main.ec_cal_sample, self.main.ec_cal_zero,
                        self.main.ph_cal_buffer4, self.main.ph_cal_buffer7,
                        self.main.tu_cal_zero, self.main.tu_cal_sample
                    ]
                }
            }

            current_sensor = self.main.sensor_type_value.text()
            
            if current_sensor in sensor_config:
                config = sensor_config[current_sensor]
                
                config["active_checkbox"].setEnabled(True)
                config["active_checkbox"].setChecked(True)
                config["active_checkbox"].setEnabled(False)
                
                for checkbox in config["inactive_checkboxes"]:
                    checkbox.setEnabled(False)
                    checkbox.setChecked(False)
                
                for button in config["active_buttons"]:
                    button.setEnabled(True)
                
                for button in config["inactive_buttons"]:
                    button.setEnabled(False)
            else:
                self.disable_all_controls()

        except Exception as e:
            self.show_error_message(f"체크박스 업데이트 중 오류 발생: {e}")
            self.disable_all_controls()

    def update_calibration_buttons(self, sensor_type):
        """Enable calibration buttons based on the selected sensor type."""
        try:
            button_mapping = {
                "TU": {
                    "enable": [self.main.tu_cal_zero, self.main.tu_cal_sample],
                    "disable": [self.main.ph_cal_buffer4, self.main.ph_cal_buffer7, 
                            self.main.cl_cal_zero, self.main.cl_cal_sample,
                            self.main.ec_cal_zero, self.main.ec_cal_sample]
                },
                "pH": {
                    "enable": [self.main.ph_cal_buffer4, self.main.ph_cal_buffer7],
                    "disable": [self.main.tu_cal_zero, self.main.tu_cal_sample,
                            self.main.cl_cal_zero, self.main.cl_cal_sample,
                            self.main.ec_cal_zero, self.main.ec_cal_sample]
                },
                "CL": {
                    "enable": [self.main.cl_cal_zero, self.main.cl_cal_sample],
                    "disable": [self.main.tu_cal_zero, self.main.tu_cal_sample,
                            self.main.ph_cal_buffer4, self.main.ph_cal_buffer7,
                            self.main.ec_cal_zero, self.main.ec_cal_sample]
                },
                "EC": {
                    "enable": [self.main.ec_cal_zero, self.main.ec_cal_sample],
                    "disable": [self.main.tu_cal_zero, self.main.tu_cal_sample,
                            self.main.ph_cal_buffer4, self.main.ph_cal_buffer7,
                            self.main.cl_cal_zero, self.main.cl_cal_sample]
                }
            }

            if sensor_type in button_mapping:
                for button in button_mapping[sensor_type]["enable"]:
                    button.setEnabled(True)
                for button in button_mapping[sensor_type]["disable"]:
                    button.setEnabled(False)
            else:
                self.disable_all_buttons()

        except Exception as e:
            self.show_error_message(f"버튼 상태 업데이트 중 오류 발생: {e}")
            self.disable_all_buttons()

    def update_status_background(self, label, color):
        """Change the background color of the specified status label."""
        try:
            color_map = {
                "green": "lightgreen",
                "red": "lightcoral"
            }
            
            background_color = color_map.get(color, "lightcoral")
            style = f"font-size: 16px; font-weight: bold; border: 2px {background_color}; padding: 5px; border-radius: 15px; background-color: {background_color}; color: white;"
            
            label_map = {
                "good": self.main.good_label,
                "error": self.main.error_label,
                "finish": self.main.finish_label
            }
            
            if label in label_map:
                label_map[label].setStyleSheet(style)

        except Exception as e:
            self.show_error_message(f"상태 배경 업데이트 중 오류 발생: {e}")

    def enable_calibration_buttons(self, enable):
        """Enable or disable calibration buttons."""
        if enable:
            self.update_checkboxes()
        else:
            self.disable_all_buttons()

    def enable_sensorRead_buttons(self, enable):
        """Enable or disable sensor read button."""
        self.main.sensor_read_button.setEnabled(enable)

    def disable_all_controls(self):
        """Disable all UI controls."""
        all_controls = [
            self.main.ec_checkbox, self.main.ph_checkbox, 
            self.main.tu_checkbox, self.main.cl_checkbox
        ] + self.get_all_buttons()

        for control in all_controls:
            control.setEnabled(False)
            if isinstance(control, QCheckBox):
                control.setChecked(False)

    def disable_all_buttons(self):
        """Disable all calibration buttons."""
        for button in self.get_all_buttons():
            button.setEnabled(False)

    def get_all_buttons(self):
        """Return list of all calibration buttons."""
        return [
            self.main.ec_cal_sample, self.main.ec_cal_zero,
            self.main.ph_cal_buffer4, self.main.ph_cal_buffer7,
            self.main.tu_cal_zero, self.main.tu_cal_sample,
            self.main.cl_cal_zero, self.main.cl_cal_sample
        ]

    def show_error_message(self, message):
        """Display error message."""
        QMessageBox.critical(self.main, "Error", message) 

    def disable_selected_checkbox(self):
        """Disable the currently selected checkbox """
        try:
            
            if self.main.tu_checkbox.isChecked():
                #self.main.tu_checkbox.setStyleSheet(style)
                self.main.tu_checkbox.setEnabled(False)
            elif self.main.ph_checkbox.isChecked():
                #self.main.ph_checkbox.setStyleSheet(style)
                self.main.ph_checkbox.setEnabled(False)
            elif self.main.cl_checkbox.isChecked():
                #self.main.cl_checkbox.setStyleSheet(style)
                self.main.cl_checkbox.setEnabled(False)
            elif self.main.ec_checkbox.isChecked():
                #self.main.ec_checkbox.setStyleSheet(style)
                self.main.ec_checkbox.setEnabled(False)
        except Exception as e:
            self.show_error_message(f"체크박스 비활성화 중 오류 발생: {e}")

    def enable_selected_checkbox(self):
        """Enable the previously selected checkbox."""
        try:
            if not self.main.tu_checkbox.isEnabled() and self.main.tu_checkbox.isChecked():
                self.main.tu_checkbox.setEnabled(True)
            elif not self.main.ph_checkbox.isEnabled() and self.main.ph_checkbox.isChecked():
                self.main.ph_checkbox.setEnabled(True)
            elif not self.main.cl_checkbox.isEnabled() and self.main.cl_checkbox.isChecked():
                self.main.cl_checkbox.setEnabled(True)
            elif not self.main.ec_checkbox.isEnabled() and self.main.ec_checkbox.isChecked():
                self.main.ec_checkbox.setEnabled(True)
        except Exception as e:
            self.show_error_message(f"체크박스 활성화 중 오류 발생: {e}") 