using System;
using System.IO.Ports;
using System.Threading.Tasks;
using Modbus.IO;
using Modbus.Data;
using Modbus.Device;
using Modbus.Serial;
using System.Collections.Generic;
using DigitalSensor.Models;
using System.Threading;

namespace DigitalSensor.Services;

public class UsbSerialAdapter : IStreamResource
{
    private readonly IUsbService _usbService;


    public UsbSerialAdapter(IUsbService usbService)
    {
        _usbService = usbService;
    }

    public int InfiniteTimeout { get; } = -1;

    public int ReadTimeout { get; set; } = 1000;
    public int WriteTimeout { get; set; } = 1000;

    public int Read(byte[] buffer, int offset, int count)
    {
        //var task = _usbService.ReadAsync(buffer, offset, count, CancellationToken.None);
        //task.Wait();
        //return task.Result;

        return _usbService.Read(buffer, offset, count);

        // 비동기 버전이 좋을 것 같지만, 안드로이드에서 문제가 많이 생김.
        //return Task.Run(() => _usbService.ReadAsync(buffer, offset, count)).GetAwaiter().GetResult();
    }
    public void Write(byte[] buffer, int offset, int count)
    {
        //var task = _usbService.WriteAsync(buffer, offset, count, CancellationToken.None);
        //task.Wait();

        _usbService.Write(buffer, offset, count);
        //Task.Run(() => _usbService.WriteAsync(buffer, offset, count)).GetAwaiter().GetResult();
    }

    public void DiscardInBuffer()
    {
        _usbService.DiscardInBuffer();
    }

    public void Dispose()
    {
        _usbService.Close();
    }

}

