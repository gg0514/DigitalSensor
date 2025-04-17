using Avalonia.Controls;
using Avalonia.Controls.Notifications;
//using UsbSerialForAndroid.Resources;

namespace DigitalSensor.Services
{
    public class NotificationService
    {
        private static WindowNotificationManager? windowNotificationManager;

        public NotificationPosition Position { get; set; } = NotificationPosition.BottomRight;        
        public int MaxItems { get; set; } = 1;

        
        public void SetTopLevel(TopLevel? topLevel)
        {
            windowNotificationManager = new WindowNotificationManager(topLevel)
            {
                Position = this.Position,
                MaxItems = this.MaxItems
            };
        }
        
        public void ShowMessage(string title, string msg, NotificationType type = NotificationType.Information)
        {
            windowNotificationManager?.Show(new Notification()
            {
                Title = title,
                Message = msg,
                Type = type
            });
        }
    }
}
