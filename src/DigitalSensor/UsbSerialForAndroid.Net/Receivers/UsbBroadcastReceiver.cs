using Android.App;
using Android.Content;
using Android.Hardware.Usb;
using Android.Widget;
using Android.Util;

using System;

namespace UsbSerialForAndroid.Net.Receivers
{
    // 권한 응답 전역 관리
    public static class UsbPermissionManager
    {
        public static UsbDevice? PendingDevice;
        public static Action<UsbDevice>? PermissionGranted;
        public static Action<UsbDevice>? PermissionDenied;
        public static Action<Exception>? PermissionError;

        internal static void OnPermissionGranted()
        {
            if (PendingDevice != null)
            {
                PermissionGranted?.Invoke(PendingDevice);
                PendingDevice = null; // 한 번 처리 후 초기화
            }
        }

        internal static void OnPermissionDenied()
        {
            if (PendingDevice != null)
            {
                PermissionDenied?.Invoke(PendingDevice);
                PendingDevice = null;
            }
        }

        internal static void OnPermissionError(Exception ex)
        {
            PermissionError?.Invoke(ex);
            PendingDevice = null;
        }
    }


    public class UsbPermissionReceiver : BroadcastReceiver
    {
        public UsbPermissionReceiver() { } // 반드시 public 기본 생성자 필요

        public override void OnReceive(Context? context, Intent? intent)
        {
            try
            {
                if (intent?.Action == UsbBroadcastReceiver.UsbPermissionAction)
                {
                    Log.Debug("DOTNET", "UsbPermissionReceiver - OnReceive");

                    bool granted = intent.GetBooleanExtra(UsbManager.ExtraPermissionGranted, false);
                    if (!granted && context?.GetSystemService(Context.UsbService) is UsbManager usbManager)
                    {
                        if (usbManager.HasPermission(UsbPermissionManager.PendingDevice))
                            granted = true; // 최종 보정
                    }

                    if (granted)
                    {
                        UsbPermissionManager.OnPermissionGranted();
                        //if (context != null)
                        //    Toast.MakeText(context, "USB permission granted", ToastLength.Short)?.Show();
                    }
                    else
                    {
                        UsbPermissionManager.OnPermissionDenied();
                        //if (context != null)
                        //    Toast.MakeText(context, "USB permission denied", ToastLength.Short)?.Show();
                    }
                }
            }
            catch (Exception ex)
            {
                UsbPermissionManager.OnPermissionError(ex);
                if (context != null)
                    Toast.MakeText(context, ex.Message, ToastLength.Long)?.Show();
            }
        }
    }


    internal class UsbBroadcastReceiver : BroadcastReceiver
    {
        public Action<UsbDevice>? UsbDeviceAttached;
        public Action<UsbDevice>? UsbDeviceDetached;
        public Action<Exception>? ErrorCallback;
        public bool IsShowToast { get; set; } = true;

        internal const string UsbPermissionAction = "com.android.example.USB_PERMISSION";

        public override void OnReceive(Context? context, Intent? intent)
        {
            try
            {
                if (context?.GetSystemService(Context.UsbService) is UsbManager usbManager &&
                    intent?.Extras?.Get(UsbManager.ExtraDevice) is UsbDevice usbDevice)
                {
                    string msg = $"PID={usbDevice.ProductId} VID={usbDevice.VendorId}";

                    switch (intent.Action)
                    {
                        case UsbManager.ActionUsbDeviceAttached:
                            {
                                Log.Debug("DOTNET", "UsbBroadcastReceiver - ActionUsbDeviceAttached");

                                msg = "USB Attached: " + msg;

                                if (usbManager.HasPermission(usbDevice))
                                {
                                    UsbDeviceAttached?.Invoke(usbDevice);
                                }
                                else
                                {
                                    // 디바이스 기억해두기
                                    UsbPermissionManager.PendingDevice = usbDevice;
                                    UsbPermissionManager.PermissionGranted = UsbDeviceAttached;
                                    UsbPermissionManager.PermissionDenied = d =>
                                    {
                                        if (IsShowToast)
                                            Toast.MakeText(context, "USB permission denied", ToastLength.Short)?.Show();
                                    };
                                    UsbPermissionManager.PermissionError = ErrorCallback;

                                    // UsbPermissionReceiver 등록
                                    var filter = new IntentFilter(UsbPermissionAction);
                                    context.RegisterReceiver(new UsbPermissionReceiver(), filter);


                                    // PendingIntent 생성 (내 앱 내부 한정)
                                    var permissionIntent = new Intent(UsbPermissionAction);
                                    var pendingIntent = PendingIntent.GetBroadcast(
                                        context,
                                        0,
                                        permissionIntent,
                                        PendingIntentFlags.Immutable);

                                    usbManager.RequestPermission(usbDevice, pendingIntent);
                                }
                                break;
                            }
                        case UsbManager.ActionUsbDeviceDetached:
                            {
                                msg = "USB Detached: " + msg;
                                UsbDeviceDetached?.Invoke(usbDevice);
                                break;
                            }
                    }

                    if (IsShowToast)
                        Toast.MakeText(context, msg, ToastLength.Short)?.Show();
                }
            }
            catch (Exception ex)
            {
                if (IsShowToast)
                    Toast.MakeText(context, ex.Message, ToastLength.Long)?.Show();
                ErrorCallback?.Invoke(ex);
            }
        }
    }
}
