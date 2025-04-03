using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;

namespace DigitalSensor.Extensions
{
    public static class HostExtension
    {
        public static T? GetService<T>(this IHost host)
        {
            return host.Services.GetService<T>();
        }
    }
}
