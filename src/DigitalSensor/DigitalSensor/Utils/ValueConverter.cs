using Avalonia.Data.Converters;
using Avalonia.Media;
using DigitalSensor.Models;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DigitalSensor.Utils;

public class EmptyStringToZeroConverter : IValueConverter
{
    public object? Convert(object? value, Type targetType, object? parameter, CultureInfo culture)
    {
        // ViewModel의 float? → TextBox.Text
        if (value is float f)
            return f.ToString(culture);

        return "0";
    }

    public object? ConvertBack(object? value, Type targetType, object? parameter, CultureInfo culture)
    {
        var text = value as string;
        if (string.IsNullOrWhiteSpace(text))
            return 0f; // 빈 문자열 → 0으로 변환

        if (float.TryParse(text, NumberStyles.Float, culture, out var result))
            return result;

        return 0f; // 변환 실패 시도 0으로
    }
}

public class BoolToBrushConverter : IValueConverter
{
    public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
    {
        if (value is bool boolValue)
        {
            // parameter가 있을 경우 (예: "Red" 전달되면 붉은색으로 변환)
            if (parameter is string colorName)
            {
                switch (colorName)
                {
                    case "Red":
                        return boolValue ? Brushes.Red : Brushes.Gray;
                    case "Orange":
                        return boolValue ? Brushes.Orange : Brushes.Gray;
                    case "GreenYellow":
                        return boolValue ? Brushes.GreenYellow : Brushes.Gray;
                    default:
                        return boolValue ? Brushes.Green : Brushes.Gray;
                }
            }

            // parameter가 없으면 기본값 사용
            return boolValue ? Brushes.Green : Brushes.Gray;
        }

        return Brushes.Gray;  // 기본값
    }

    public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
    {
        throw new NotImplementedException();
    }
}

public class EnumDescriptionConverter : IValueConverter
{
    public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
    {
        if (value is Enum enumValue)
        {
            return enumValue.GetDescription();
        }
        return value?.ToString();
    }

    public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
    {
        throw new NotImplementedException();
    }
}

public class StatusDescriptionConverter : IValueConverter
{
    public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
    {
        if (value is Enum enumValue)
        {
            return enumValue.GetLocalizedDescription();
        }
        return value?.ToString();
    }

    public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
    {
        throw new NotImplementedException();
    }
}


public static class UnitMapper
{
    public static readonly Dictionary<SensorType, string> Units = new()
    {
        { SensorType.None, "" },
        { SensorType.TurbidityLow, "NTU" },
        { SensorType.TurbidityHighIR, "NTU" },
        { SensorType.TurbidityHighColor, "NTU" },
        { SensorType.PH, "pH" },
        { SensorType.ContactingConductivity, "㎲/㎝" },
        { SensorType.NonContactingConductivity, "㎲/㎝" },
        { SensorType.Chlorine, "㎎/l" },
    };
}
