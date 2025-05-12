using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Resources;
using System.Text;
using System.Threading.Tasks;

namespace DigitalSensor.Resources;

public static class LocalizationManager
{
    public static CultureInfo CurrentCulture { get; private set; } = CultureInfo.CurrentUICulture;

    private static readonly ResourceManager _resourceManager =
        new ResourceManager("DigitalSensor.Resources.Strings", typeof(LocalizationManager).Assembly);


    public static string GetString(string key)
    {
        return _resourceManager.GetString(key, CurrentCulture) ?? $"!{key}!";
    }


    public static void SetCulture(string cultureCode)
    {
        CurrentCulture = new CultureInfo(cultureCode);
    }
}
