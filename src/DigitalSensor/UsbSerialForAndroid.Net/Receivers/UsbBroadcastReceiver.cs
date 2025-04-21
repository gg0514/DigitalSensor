using Android.App;
using Android.Content;
using Android.Hardware.Usb;
using Android.Widget;
using System;

namespace UsbSerialForAndroid.Net.Receivers
{
    internal class UsbBroadcastReceiver : BroadcastReceiver
    {
        public Action<UsbDevice>? UsbDeviceAttached;
        public Action<UsbDevice>? UsbDeviceDetached;
        public Action<Exception>? ErrorCallback;
        public bool IsShowToast { get; set; } = true;

        private const string UsbPermissionAction = "com.example.USB_PERMISSION";

        public override void OnReceive(Context? context, Intent? intent)
        {
            try
            {
                var usbService = context?.GetSystemService(Context.UsbService);
                if (usbService is UsbManager usbManager && intent is not null && intent.Extras is not null)
                {
                    if (intent.Extras.Get(UsbManager.ExtraDevice) is UsbDevice usbDevice)
                    {
                        string msg = $"PID={usbDevice.ProductId} VID={usbDevice.VendorId}";
                        switch (intent.Action)
                        {
                            case UsbManager.ActionUsbDeviceAttached:
                                {
                                    msg = AppResources.UsbDeviceAttached + msg;
                                    if (usbManager.HasPermission(usbDevice))
                                    {
                                        UsbDeviceAttached?.Invoke(usbDevice);
                                    }
                                    else
                                    {
                                        var permissionIntent = new Intent(UsbPermissionAction);
                                        var pendingIntent = PendingIntent.GetBroadcast(context, 0, permissionIntent, PendingIntentFlags.Immutable);

                                        // 여기서 USB 권한 요청
                                        context.RegisterReceiver(new UsbPermissionReceiver(usbDevice, UsbDeviceAttached, ErrorCallback, IsShowToast), new IntentFilter(UsbPermissionAction));
                                        usbManager.RequestPermission(usbDevice, pendingIntent);
                                    }
                                    break;
                                }
                            case UsbManager.ActionUsbDeviceDetached:
                                {
                                    msg = AppResources.UsbDeviceDetached + msg;

                                    // 여기서 USB Device가 분리되었음을 알린다.
                                    UsbDeviceDetached?.Invoke(usbDevice);
                                    break;
                                }
                            default:
                                break;
                        }

                        if (IsShowToast)
                            Toast.MakeText(context, msg, ToastLength.Short)?.Show();
                    }
                }
            }
            catch (Exception ex)
            {
                if (IsShowToast)
                    Toast.MakeText(context, ex.Message, ToastLength.Long)?.Show();
                ErrorCallback?.Invoke(ex);
            }
        }

        private class UsbPermissionReceiver : BroadcastReceiver
        {
            private readonly UsbDevice usbDevice;
            private readonly Action<UsbDevice>? usbDeviceAttachedCallback;
            private readonly Action<Exception>? errorCallback;
            private readonly bool isShowToast;

            public UsbPermissionReceiver(UsbDevice usbDevice, Action<UsbDevice>? usbDeviceAttachedCallback, Action<Exception>? errorCallback, bool isShowToast)
            {
                this.usbDevice = usbDevice;
                this.usbDeviceAttachedCallback = usbDeviceAttachedCallback;
                this.errorCallback = errorCallback;
                this.isShowToast = isShowToast;
            }

            public override void OnReceive(Context? context, Intent? intent)
            {
                try
                {
                    if (intent?.Action == UsbPermissionAction)
                    {
                        bool granted = intent.GetBooleanExtra(UsbManager.ExtraPermissionGranted, true);
                        if (granted)
                        {
                            // 여기서 USB 권한을 획득한후에 콜백함수를 호출한다.
                            usbDeviceAttachedCallback?.Invoke(usbDevice);

                            if (isShowToast)
                                Toast.MakeText(context, "USB permission granted", ToastLength.Short)?.Show();
                        }
                        else
                        {
                            if (isShowToast)
                                Toast.MakeText(context, "USB permission denied", ToastLength.Short)?.Show();
                        }
                    }
                }
                catch (Exception ex)
                {
                    if (isShowToast)
                        Toast.MakeText(context, ex.Message, ToastLength.Long)?.Show();
                    errorCallback?.Invoke(ex);
                }
                finally
                {
                    context?.UnregisterReceiver(this);
                }
            }
        }

    }
}
