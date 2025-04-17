using System;
using System.IO.Ports;
using System.Threading.Tasks;
using Modbus.IO;
using Modbus.Data;
using Modbus.Device;
using Modbus.Serial;
using System.Collections.Generic;
using DigitalSensor.Models;
using DigitalSensor.Services;


namespace DigitalSensor.Modbus;

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
        return _usbService.Read(buffer, offset, count);
    }
    public void Write(byte[] buffer, int offset, int count)
    {
        _usbService.Write(buffer, offset, count);
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

