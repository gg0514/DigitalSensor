using DigitalSensor.Resources;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using DigitalSensor.Resources;
using System.Globalization;
using System.Resources;

namespace DigitalSensor.Models;


public enum CalibrationStatus
{
    [Description("대 기")]
    NoSensorCalibration = 0,
    [Description("진 행")]
    CalInProgress = 1,
    [Description("성 공")]
    CalOK = 2,
    [Description("실 패 : 3")]
    Fail_NotStable = 3,
    [Description("실 패 : 4")]
    Fail_BufferNotFound = 4,
    [Description("실 패 : 5")]
    Fail_1BufferNotFound = 5,
    [Description("실 패 : 6")]
    Fail_2BufferNotFound = 6,
    [Description("실 패 : 7")]
    Fail_ValueTooLow = 7,
    [Description("실 패 : 8")]
    Fail_ValueTooHigh = 8,
    [Description("실 패 : 9")]
    Fail_SlopeTooLow = 9,
    [Description("실 패 : 10")]
    Fail_SlopeTooHigh = 10,
    [Description("실 패 : 11")]
    Fail_OffsetTooLow = 11,
    [Description("실 패 : 12")]
    Fail_OffsetTooHigh = 12,
    [Description("실 패 : 13")]
    Fail_PointsTooClose = 13,
    [Description("실 패 : 14")]
    Fail_GeneralCalFail = 14
}

public enum CommandStatus
{
    Ready = 0,
    Running= 1,
    Completed = 2
}

public enum SensorType
{
    [Description("None")]
    None = 0,
    [Description("pH")]
    PH = 1,
    ORP = 2,                // ORP 센서
    DO = 3,                 // 용존산소 센서    
    [Description("EC")]
    ContactingConductivity = 4,             // 접촉식 전도도 센서
    NonContactingConductivity = 5,          // 비접촉식 전도도 센서
    Ozone = 6,                              // 오존 센서
    [Description("TU")]
    TurbidityLow = 7,
    [Description("TU")]
    TurbidityHighColor = 8,
    [Description("TU")]
    TurbidityHighIR = 9,
    [Description("CL")]
    Chlorine = 10,
    SuspendedSolids = 11    // 부유물질 센서
}


public static class EnumExtensions
{

    public static string GetDescription(this Enum value)
    {
        FieldInfo field = value.GetType().GetField(value.ToString());
        DescriptionAttribute attribute = field.GetCustomAttribute<DescriptionAttribute>();
        return attribute == null ? value.ToString() : attribute.Description;
    }


    public static string GetLocalizedDescription(this Enum value)
    {
        // 키는 enum 이름으로 결정 (예: CalibrationStatus.Fail_SlopeTooLow)
        //string resourceKey = $"{value.GetType().Name}_{value}";

        string resourceKey = "StatusReady";

        if (value is CalibrationStatus status)
        {
            if (status == CalibrationStatus.NoSensorCalibration)
                resourceKey = "StatusReady";
            else if (status == CalibrationStatus.CalInProgress)
                resourceKey = "StatusProgress";
            else if (status == CalibrationStatus.CalOK)
                resourceKey = "StatusSuccess";
            else if (status == CalibrationStatus.Fail_ValueTooLow)
                resourceKey = "StatusFail_ValueTooLow";
            else if (status == CalibrationStatus.Fail_ValueTooHigh)
                resourceKey = "StatusFail_ValueTooHigh";
            else if (status == CalibrationStatus.Fail_SlopeTooLow)
                resourceKey = "StatusFail_SlopeTooLow";
            else if (status == CalibrationStatus.Fail_SlopeTooHigh)
                resourceKey = "StatusFail_SlopeTooHigh";
            else if (status == CalibrationStatus.Fail_OffsetTooLow)
                resourceKey = "StatusFail_OffsetTooLow";
            else if (status == CalibrationStatus.Fail_OffsetTooHigh)
                resourceKey = "StatusFail_OffsetTooHigh";
            else if (status == CalibrationStatus.Fail_PointsTooClose)
                resourceKey = "StatusFail_PointsTooClose";
            else
                resourceKey = "StatusFail";
        }

        return LocalizationManager.GetString(resourceKey);
    }
}