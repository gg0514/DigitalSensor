
namespace DigitalSensor.Resources;

public class Localize
{
    public string this[string key] => LocalizationManager.GetString(key);
}