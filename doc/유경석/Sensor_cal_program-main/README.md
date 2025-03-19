# Sensor Calibration 테스트 프로그램(ASCII version)
## version 1.0.0
## 1. Source

### &nbsp; - main.py : UI design
### &nbsp; - ui_controller.py : UI setting
### &nbsp; - ec_calibration.py : EC calibration action
### &nbsp; - ph_calibration.py : pH calibration action
### &nbsp; - cl_calibration.py : CL calibration action


## 2. 설명
### &nbsp; - 각 센서 데이터 READ button을 통해 확인 가능
### &nbsp; - 각 센서 마다 calibration Step에 따라 동작 확인
### &nbsp; - 시리얼 통신연결을 통해 데이터 전송(ASCII), 명령뒤 Carriage Return 적용(CR)

## 3. 센서 데이터 전송
### &nbsp; - GSTYPE\r : 센서 타입 확인 => return : 04 (EC) or 01 (pH) or 07 (TU) or 10 (CL) 
### &nbsp; - CSNSR 1\r : 센서 데이터 1초에 한번 READ => parameters : secs(1~120)  => return : counts mvolts value (ex :2282264 348.25 0.003)
### &nbsp; - ESCAPE\r : 센서 데이터 READ STOP => return : ERROR


## 4. 캘리브레이션 데이터 전송
### &nbsp; - CALZERO\r : Zero Calibration
### &nbsp; - CALS1PS\r : 1 Point Sample Calibration
### &nbsp; - CALS2PB 0\r : 2 Point Buffer Calibration(pH 4)
### &nbsp; - CALS2PB 1\r : 2 Point Buffer Calibration(pH 7)
#### (1). pH 센서 (status[0] = '01'):
#### &nbsp; - 2 Point Buffer pH 4.0 캘리브레이션 (CALS2PB 0\r) => pH4.0 시료수에 대한 캘리브레이션
#### &nbsp; - 2 Point Buffer pH 7.0 캘리브레이션 (CALS2PB 1\r) => pH7.0 시료수에 대한 캘리브레이션
#### (2). EC 센서 (status[0] = '04'):
#### &nbsp; - Zero 캘리브레이션 (CALSZERO\r)
#### &nbsp; - 1 Point Sample 캘리브레이션 (CALS1PS 1413\r)
#### (3). CL 센서 (status[0] = '10'):
#### &nbsp; - Zero 캘리브레이션 (CALSZERO\r)
#### &nbsp; - 1 Point Sample 캘리브레이션 (CALS1PS 1.8\r)


## 5. CALSTATUS\r : 캘리브레이션 상태 확인 
#### &nbsp;&nbsp;&nbsp; return(ex) : &nbsp; 04 04 01
#### &nbsp;&nbsp;&nbsp; => sensor type '04': EC 센서 식별자 => 현재 연결된 센서 타입 확인
#### &nbsp;&nbsp;&nbsp; => calibration type '04': 1 Point Sample => 현재 진행중인 calibration 타입 확인
#### &nbsp;&nbsp;&nbsp; => calibration status '01': Cal in progress => 현재 진행중이 calibration 진행 사항 확인

### sensor type
### &nbsp; 0 = No sensor calibration
### &nbsp; 1 = pH 
### &nbsp; 2 = ORP 
### &nbsp; 3 = DO 
### &nbsp; 4 = Contaction Conductivity Sensor : EC
### &nbsp; 5 = Toroidal Conductivity Sensor
### &nbsp; 6 = Ozone Sensor
### &nbsp; 7 = Low-Range Turbidity Sensor : TU
### &nbsp; 8 = High-Rng Turb(Argus color) Sensor(not used)
### &nbsp; 9 = High-Range Turbidity(Argus IR) Sensor
### &nbsp; 10 = Chlorine Sensor : CL
### &nbsp; 11 = Suspended Solids

## calibration type
### &nbsp; 0 = No Sensor Calibration
### &nbsp; 1 = 1pt Buffer
### &nbsp; 2 = 2pt Buffer, 1st point
### &nbsp; 3 = 2pt Buffer, 2nd point 
### &nbsp; 4 = 1pt Sample
### &nbsp; 5 = 2pt Sample, 1st point
### &nbsp; 6 = 2pt Sample, 2nd point
### &nbsp; 7 = Air Calibration
### &nbsp; 8 = Zero Calibration
### &nbsp; 10 = Temperature Calibration 1pt Sample

## calibration status
### &nbsp; 0 = No Sensor Calibration
### &nbsp; 1 = Cal in progress
### &nbsp; 2 = Cal OK
### &nbsp; 3 = Fail - Not Stable
### &nbsp; 4 = Fail - Buffer not found
### &nbsp; 5 = Fail - 1st buffer not found
### &nbsp; 6 = Fail - 2nd buffer not found
### &nbsp; 7 = Fail - Value too low
### &nbsp; 8 = Fail - Value too high
### &nbsp; 9 = Fail - Slope too low
### &nbsp; 10 = Fail - Slope too high
### &nbsp; 11 = Fail - Offset too low
### &nbsp; 12 = Fail - Offset too high
### &nbsp; 13 = Fail - Points too close
### &nbsp; 14 = General Cal Fail(zeor or sample)

 