using Android.App;
using Android.Content;
using Android.Hardware.Usb;
using System;
using System.Buffers;
using System.Net;
using System.Threading;
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
        public const int DefaultTimeout = 1000;
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
        private readonly SemaphoreSlim _semaphore = new SemaphoreSlim(1, 1);

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
            var readBuffer = ArrayPool<byte>.Shared.Rent(count);
            Array.Clear(readBuffer, 0, count); // count까지만 초기화

            int totalBytes = 0;
            int expectedLength = 0;

            try
            {
                while (totalBytes < count)
                {
                    int remaining = count - totalBytes;
                    int numBytesRead = UsbDeviceConnection.BulkTransfer(UsbEndpointRead, readBuffer, remaining, ReadTimeout);
                    if (numBytesRead < 0)
                    {
                        // This sucks: we get -1 on timeout, not 0 as preferred.
                        // We *should* use UsbRequest, except it has a bug/api oversight
                        // where there is no way to determine the number of bytes read
                        // in response :\ -- http://b.android.com/28023
                        throw new BulkTransferException("Read failed", numBytesRead, UsbEndpointRead, readBuffer, 0, count, ReadTimeout);
                    }
                    if (numBytesRead == 0)
                    {
                        // No more data to read
                        return totalBytes;
                    }

                    Array.Copy(readBuffer, 0, buffer, offset + totalBytes, numBytesRead);
                    totalBytes += numBytesRead;
                }
                return totalBytes;
            }
            finally
            {
                ArrayPool<byte>.Shared.Return(readBuffer);
            }

            return totalBytes;
        }



        // 오리지널 
        //public virtual int Read(byte[] buffer, int offset, int count)
        //{
        //    ArgumentNullException.ThrowIfNull(UsbDeviceConnection);
        //    var readBuffer = ArrayPool<byte>.Shared.Rent(count);

        //    int totalBytes = 0;
        //    int expectedLength = 0;

        //    try
        //    {
        //        while (true)
        //        {
        //            int bytesRead = UsbDeviceConnection.BulkTransfer(UsbEndpointRead, readBuffer, totalBytes, count - totalBytes, ReadTimeout);

        //            if (bytesRead < 0)
        //            {
        //                throw new BulkTransferException("Read failed", bytesRead, UsbEndpointRead, readBuffer, 0, count, ReadTimeout);
        //            }

        //            totalBytes += bytesRead;

        //            if (totalBytes >= 3 && expectedLength == 0)
        //            {
        //                expectedLength = 3 + readBuffer[2] + 2;
        //            }

        //            if (expectedLength > 0 && totalBytes >= expectedLength)
        //            {
        //                break; // 패킷 완성
        //            }

        //            if (totalBytes >= count)
        //            {
        //                break; // 버퍼가 꽉 찼음
        //            }
        //        }

        //        Array.Copy(readBuffer, 0, buffer, offset, totalBytes);
        //        //throw new BulkTransferException("Read failed", totalBytes, UsbEndpointRead, buffer, expectedLength, count, ReadTimeout);
        //    }
        //    finally
        //    {
        //        ArrayPool<byte>.Shared.Return(readBuffer);
        //    }

        //    return totalBytes;
        //}

        //************************************************************************
        // 비동기버전 (WriteAsync, ReadAsync)
        //************************************************************************

        //public virtual void Write(byte[] buffer, int offset, int size)
        //{
        //    WriteAsync(buffer, offset, size).GetAwaiter().GetResult(); // Synchronous for NModbus
        //}

        //public virtual int Read(byte[] buffer, int offset, int size)
        //{
        //    return ReadAsync(buffer, offset, size).GetAwaiter().GetResult(); // Synchronous for NModbus
        //}

        public async Task WriteAsync(byte[] buffer, int offset, int size)
        {
            if (buffer == null) throw new ArgumentNullException(nameof(buffer));
            if (offset < 0 || size < 0 || offset + size > buffer.Length)
                throw new ArgumentOutOfRangeException();

            await _semaphore.WaitAsync();
            try
            {
                int bytesTransferred = await UsbDeviceConnection.BulkTransferAsync(
                                                UsbEndpointWrite, buffer, offset, size, WriteTimeout);
                if (bytesTransferred < 0)
                    throw new InvalidOperationException("Bulk transfer failed.");
                if (bytesTransferred != size)
                    throw new InvalidOperationException($"Incomplete write: {bytesTransferred} of {size} bytes.");
            }
            finally
            {
                _semaphore.Release();
            }
        }

        public async Task<int> ReadAsync(byte[] buffer, int offset, int size)
        {
            if (buffer == null) throw new ArgumentNullException(nameof(buffer));
            if (offset < 0 || size < 0 || offset + size > buffer.Length)
                throw new ArgumentOutOfRangeException();

            await _semaphore.WaitAsync();
            try
            {
                int bytesTransferred = await UsbDeviceConnection.BulkTransferAsync(UsbEndpointRead, buffer, offset, size, ReadTimeout);
                if (bytesTransferred < 0)
                    throw new InvalidOperationException("Bulk transfer failed.");
                return bytesTransferred;
            }
            finally
            {
                _semaphore.Release();
            }
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