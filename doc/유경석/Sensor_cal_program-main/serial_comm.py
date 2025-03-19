# serial_comm.py

from PySide6.QtSerialPort import QSerialPort, QSerialPortInfo
from PySide6.QtCore import QObject, Signal

class SerialComm(QObject):
    # Define a signal for received data
    data_received = Signal(str)

    def __init__(self):
        super().__init__()
        self.serial = QSerialPort()
        self.serial.readyRead.connect(self.read_data)

    def available_ports(self):
        """Returns a list of available serial port names."""
        return [port.portName() for port in QSerialPortInfo.availablePorts()]

    def connect_serial(self, port_name, baud_rate):
        """Connects to the serial port with the given port name and baud rate."""
        self.serial.setPortName(port_name)
        self.serial.setBaudRate(baud_rate)
        
        if self.serial.open(QSerialPort.ReadWrite):
            print(f"Connected to {port_name} at {baud_rate} baud")
            return True
        else:
            print(f"Failed to connect to {port_name}")
            return False

    def disconnect_serial(self):
        """Disconnects the serial port."""
        if self.serial.isOpen():
            self.serial.close()
            print("Disconnected from serial port")

    def read_data(self):
        """Reads data from the serial port and emits it as a signal."""
        if self.serial.canReadLine():
            data = self.serial.readLine().data().decode().strip()
            self.data_received.emit(data)

    def send_data(self, data):
        """Sends data to the serial port if it’s open."""
        if self.serial.isOpen():
            self.serial.write(data.encode())
        else:
            print("Serial port is not open")
