using Microsoft.Extensions.DependencyInjection;

namespace DigitalSensor.Extensions
{
    public static class ServiceCollectionExtension
    {
        // *** 확장메소드 정의
        // 기존 IServiceCollection 인터페이스에 RegisterTransient 메소드를 추가합니다.

        public static void RegisterTransient<TView, TViewModel>(this IServiceCollection services)
            where TView : class
            where TViewModel : class
        {
            services.AddTransient<TView>();
            services.AddTransient<TViewModel>();
        }
        public static void RegisterSingleton<TView, TViewModel>(this IServiceCollection services)
           where TView : class
           where TViewModel : class
        {
            services.AddSingleton<TView>();
            services.AddSingleton<TViewModel>();
        }
    }
}
