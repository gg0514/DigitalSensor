using Android.App;
using Android.Content;
using Android.Hardware.Usb;
using System;
using System.Buffers;
using System.Threading.Tasks;
using UsbSerialForAndroid.Net.Enums;
using UsbSerialForAndroid.Net.Exceptions;

namespace UsbSerialForAndroid.Net.Drivers
{
    public abstract class UsbDriverBase
    {
        private static readonly UsbManager usbManager = GetUsbManager();
        public const byte XON = 17;
        public const byte XOFF = 19;
        public const int DefaultTimeout = 5000;
        public const int DefaultBufferLength = 1024 * 4;
        public const int DefaultBaudRate = 9600;
        public const byte DefaultDataBits = 8;
        public const StopBits DefaultStopBits = StopBits.One;
        public const Parity DefaultParity = Parity.None;
        public const int DefaultUsbInterfaceIndex = 0;

        public FlowControl FlowControl { get; protected set; }
        public bool DtrEnable { get; protected set; }
        public bool RtsEnable { get; protected set; }
        public int UsbInterfaceIndex { get; set; } = DefaultUsbInterfaceIndex;
        public static UsbManager UsbManager => usbManager;
        public UsbDevice UsbDevice { get; private set; }
        public UsbDeviceConnection? UsbDeviceConnection { get; protected set; }
        public UsbInterface? UsbInterface { get; protected set; }
        public UsbEndpoint? UsbEndpointRead { get; protected set; }
        public UsbEndpoint? UsbEndpointWrite { get; protected set; }
        public int ReadTimeout { get; set; } = DefaultTimeout;
        public int WriteTimeout { get; set; } = DefaultTimeout;
        public int ControlTimeout { get; set; } = DefaultTimeout;
        public bool Connected => TestConnection();

        protected UsbDriverBase(UsbDevice _usbDevice)
        {
            UsbDevice = _usbDevice;
        }
        
        private static UsbManager GetUsbManager()
        {
            var usebService = Application.Context.GetSystemService(Context.UsbService);
            return usebService is UsbManager manager
                ? manager
                : throw new NullReferenceException("UsbManager is null");
        }

        public abstract void SetDtrEnable(bool value);
        public abstract void SetRtsEnable(bool value);

        public abstract void Open(int baudRate, byte dataBits, StopBits stopBits, Parity parity);

        public virtual void Close()
        {
            UsbDeviceConnection?.Close();
        }

        public virtual void Write(byte[] buffer)
        {
            ArgumentNullException.ThrowIfNull(UsbDeviceConnection);
            int result = UsbDeviceConnection.BulkTransfer(UsbEndpointWrite, buffer, 0, buffer.Length, WriteTimeout);
            if (result < 0)
                throw new BulkTransferException("Write failed", result, UsbEndpointWrite, buffer, 0, buffer.Length, WriteTimeout);
        }

        public virtual byte[]? Read()
        {
            ArgumentNullException.ThrowIfNull(UsbDeviceConnection);
            var buffer = ArrayPool<byte>.Shared.Rent(DefaultBufferLength);
            try
            {
                int result = UsbDeviceConnection.BulkTransfer(UsbEndpointRead, buffer, 0, DefaultBufferLength, ReadTimeout);
                return result >= 0
                    ? buffer.AsSpan().Slice(0, result).ToArray()
                    : default;
            }
            finally
            {
                ArrayPool<byte>.Shared.Return(buffer);
            }
        }

        public virtual async Task WriteAsync(byte[] buffer)
        {
            ArgumentNullException.ThrowIfNull(UsbDeviceConnection);
            int result = await UsbDeviceConnection.BulkTransferAsync(UsbEndpointWrite, buffer, 0, buffer.Length, WriteTimeout);
            if (result < 0)
                throw new BulkTransferException("Write failed", result, UsbEndpointWrite, buffer, 0, buffer.Length, WriteTimeout);
        }

        public virtual async Task<byte[]?> ReadAsync()
        {
            ArgumentNullException.ThrowIfNull(UsbDeviceConnection);
            var buffer = ArrayPool<byte>.Shared.Rent(DefaultBufferLength);
            try
            {
                int result = await UsbDeviceConnection.BulkTransferAsync(UsbEndpointRead, buffer, 0, DefaultBufferLength, ReadTimeout);
                return result >= 0
                    ? buffer.AsSpan().Slice(0, result).ToArray()
                    : default;
            }
            finally
            {
                ArrayPool<byte>.Shared.Return(buffer);
            }
        }


        //************************************************************************
        // 동기버전 (Write, Read)
        //************************************************************************

        public virtual void Write(byte[] buffer, int offset, int count)
        {
            ArgumentNullException.ThrowIfNull(UsbDeviceConnection);
            int result = UsbDeviceConnection.BulkTransfer(UsbEndpointWrite, buffer, offset, count, WriteTimeout);

            if (result < 0)
                throw new BulkTransferException("Write failed", result, UsbEndpointWrite, buffer, 0, buffer.Length, WriteTimeout);
        }
        public virtual int Read(byte[] buffer, int offset, int count)
        {
            ArgumentNullException.ThrowIfNull(UsbDeviceConnection);
            var readBuffer = ArrayPool<byte>.Shared.Rent(DefaultBufferLength);

            int totalBytes = 0;
            int expectedLength = 0;

            try
            {
                while (true)
                {
                    int bytesRead = UsbDeviceConnection.BulkTransfer(UsbEndpointRead, readBuffer, totalBytes, count - totalBytes, ReadTimeout);

                    if (bytesRead < 0)
                    {
                        throw new BulkTransferException("Read failed", bytesRead, UsbEndpointRead, readBuffer, 0, count, ReadTimeout);
                    }

                    totalBytes += bytesRead;

                    if (totalBytes >= 3 && expectedLength == 0)
                    {
                        expectedLength = 3 + readBuffer[2] + 2;
                    }

                    if (expectedLength > 0 && totalBytes >= expectedLength)
                    {
                        break; // 패킷 완성
                    }

                    if (totalBytes >= count)
                    {
                        break; // 버퍼가 꽉 찼음
                    }
                }

                Array.Copy(readBuffer, 0, buffer, offset, totalBytes);
                //throw new BulkTransferException("Read failed", totalBytes, UsbEndpointRead, buffer, expectedLength, count, ReadTimeout);
            }
            finally
            {
                ArrayPool<byte>.Shared.Return(readBuffer);
            }

            return totalBytes;
        }

        //************************************************************************
        // 비동기버전 (WriteAsync, ReadAsync)
        //************************************************************************

        public virtual async Task WriteAsync(byte[] buffer, int offset, int count)
        {
            ArgumentNullException.ThrowIfNull(UsbDeviceConnection);

            int result = await UsbDeviceConnection.BulkTransferAsync(
                UsbEndpointWrite, buffer, offset, count, WriteTimeout);

            if (result < 0)
                throw new BulkTransferException("WriteAsync failed", result, UsbEndpointWrite, buffer, 0, buffer.Length, WriteTimeout);
        }

        public async Task<int> ReadAsync(byte[] buffer, int offset, int count)
        {
            var readBuffer = ArrayPool<byte>.Shared.Rent(DefaultBufferLength);
            int totalBytes = 0;
            int expectedLength = 0;

            try
            {
                while (true)
                {
                    int bytesRead = await UsbDeviceConnection.BulkTransferAsync(UsbEndpointRead, readBuffer, totalBytes, count - totalBytes, ReadTimeout);

                    if (bytesRead < 0)
                        throw new BulkTransferException("ReadAsync failed", bytesRead, UsbEndpointRead, readBuffer, 0, count, ReadTimeout);

                    totalBytes += bytesRead;

                    if (totalBytes >= 3 && expectedLength == 0)
                        expectedLength = 3 + readBuffer[2] + 2;

                    if (expectedLength > 0 && totalBytes >= expectedLength)
                        break;

                    if (totalBytes >= count)
                        break;
                }

                Array.Copy(readBuffer, 0, buffer, offset, totalBytes);
            }
            finally
            {
                ArrayPool<byte>.Shared.Return(readBuffer);
            }

            return totalBytes;
        }

        public static UsbInterface[] GetUsbInterfaces(UsbDevice usbDevice)
        {
            var array = new UsbInterface[usbDevice.InterfaceCount];
            for (int i = 0; i < usbDevice.InterfaceCount; i++)
            {
                array[i] = usbDevice.GetInterface(i);
            }
            return array;
        }

        public bool TestConnection()
        {
            try
            {
                ArgumentNullException.ThrowIfNull(UsbDeviceConnection);
                byte[] buf = new byte[2];
                const int request = 0;//GET_STATUS
                int len = UsbDeviceConnection.ControlTransfer(UsbAddressing.DirMask, request, 0, 0, buf, buf.Length, 100);
                return len == 2;
            }
            catch
            {
                return false;
            }
        }
    }
}