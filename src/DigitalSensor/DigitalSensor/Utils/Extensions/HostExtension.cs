using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;

namespace DigitalSensor.Extensions
{
    public static class HostExtension
    {
        // *** 확장메소드 정의
        // 기존 IHost 인터페이스에 GetService<T> 메소드를 추가합니다.
        public static T? GetService<T>(this IHost host)
        {
            return host.Services.GetService<T>();
        }
    }
}
