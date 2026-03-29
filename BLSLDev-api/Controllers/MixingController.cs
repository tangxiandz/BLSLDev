using BLSLDev_api.Data;
using BLSLDev_api.Models;
using Microsoft.AspNetCore.Mvc;

namespace BLSLDev_api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class MixingController : ControllerBase
    {
        private readonly BLSLDbContext _dbContext;

        public MixingController(BLSLDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        // 获取所有物料列表
        [HttpGet("materials")]
        public IActionResult GetAllMaterials()
        {
            var materials = _dbContext.SemiProductMaterials
                .Select(spm => new {
                    spm.Id,
                    spm.SemiProductCode,
                    spm.SemiProductDesc,
                    spm.RawMaterialCode,
                    spm.RawMaterialDesc,
                    spm.Quantity
                })
                .ToList();

            return Ok(materials);
        }

        // 获取半成品对应的物料信息列表
        [HttpGet("materials/{semiProductCode}")]
        public IActionResult GetMaterialsBySemiProduct(string semiProductCode)
        {
            var materials = _dbContext.SemiProductMaterials
                .Where(spm => spm.SemiProductCode == semiProductCode)
                .Select(spm => new {
                    spm.RawMaterialCode,
                    spm.RawMaterialDesc,
                    spm.Quantity
                })
                .ToList();

            return Ok(materials);
        }

        // 添加物料
        [HttpPost("material")]
        public IActionResult AddMaterial([FromBody] SemiProductMaterial material)
        {
            if (material == null)
            {
                return BadRequest("Invalid request body");
            }

            material.CreatedAt = DateTime.Now;
            material.UpdatedAt = DateTime.Now;
            
            _dbContext.SemiProductMaterials.Add(material);
            _dbContext.SaveChanges();

            return Ok(new { Message = "物料添加成功", Material = material });
        }

        // 更新物料
        [HttpPut("material/{id}")]
        public IActionResult UpdateMaterial(int id, [FromBody] SemiProductMaterial material)
        {
            if (material == null)
            {
                return BadRequest("Invalid request body");
            }

            var existingMaterial = _dbContext.SemiProductMaterials.Find(id);
            if (existingMaterial == null)
            {
                return NotFound("物料不存在");
            }

            existingMaterial.SemiProductCode = material.SemiProductCode;
            existingMaterial.SemiProductDesc = material.SemiProductDesc;
            existingMaterial.RawMaterialCode = material.RawMaterialCode;
            existingMaterial.RawMaterialDesc = material.RawMaterialDesc;
            existingMaterial.Quantity = material.Quantity;
            existingMaterial.UpdatedAt = DateTime.Now;

            _dbContext.SaveChanges();

            return Ok(new { Message = "物料更新成功", Material = existingMaterial });
        }

        // 删除物料
        [HttpDelete("material/{id}")]
        public IActionResult DeleteMaterial(int id)
        {
            var material = _dbContext.SemiProductMaterials.Find(id);
            if (material == null)
            {
                return NotFound("物料不存在");
            }

            _dbContext.SemiProductMaterials.Remove(material);
            _dbContext.SaveChanges();

            return Ok(new { Message = "物料删除成功" });
        }

        // 提交拌料记录
        [HttpPost("record")]
        public IActionResult SubmitMixingRecord([FromBody] MixingRecord record)
        {
            if (record == null)
            {
                return BadRequest("Invalid request body");
            }

            record.OperationTime = DateTime.Now;
            
            _dbContext.MixingRecords.Add(record);
            _dbContext.SaveChanges();

            return Ok(new { Message = "拌料记录提交成功" });
        }

        // 获取所有拌料记录
        [HttpGet("records")]
        public IActionResult GetMixingRecords()
        {
            var records = _dbContext.MixingRecords
                .OrderByDescending(r => r.OperationTime)
                .Select(r => new {
                    r.Id,
                    r.SemiProductCode,
                    r.RawMaterialCode,
                    r.BucketCode,
                    r.Quantity,
                    OperationTime = r.OperationTime.ToString("yyyy-MM-dd HH:mm:ss")
                })
                .ToList();

            return Ok(records);
        }

        // 导入物料
        [HttpPost("import/materials")]
        public async Task<IActionResult> ImportMaterials(IFormFile file)
        {
            if (file == null || file.Length == 0)
            {
                return BadRequest("No file uploaded");
            }

            try
            {
                var importedCount = 0;
                var duplicateCount = 0;
                var failedCount = 0;
                var errorLogs = new List<string>();

                using (var stream = new StreamReader(file.OpenReadStream(), System.Text.Encoding.UTF8))
                {
                    // 跳过表头
                    var headerLine = await stream.ReadLineAsync();
                    if (string.IsNullOrWhiteSpace(headerLine))
                    {
                        return Ok(new {
                            Message = "物料导入成功",
                            ImportedCount = 0,
                            DuplicateCount = 0,
                            FailedCount = 1,
                            ErrorLogs = new List<string> { "文件没有表头" }
                        });
                    }

                    string line;
                    int lineNumber = 2; // 从第二行开始计算
                    while ((line = await stream.ReadLineAsync()) != null)
                    {
                        if (string.IsNullOrWhiteSpace(line)) 
                        {
                            lineNumber++;
                            continue;
                        }

                        try
                        {
                            var parts = line.Split(',');
                            if (parts.Length >= 5)
                            {
                                var semiProductCode = parts[0].Trim();
                                var semiProductDesc = parts[1].Trim();
                                var rawMaterialCode = parts[2].Trim();
                                var rawMaterialDesc = parts[3].Trim();
                                var quantity = parts[4].Trim();

                                // 检查是否已存在相同的半成品料号和原料号
                                var existingMaterial = _dbContext.SemiProductMaterials
                                    .FirstOrDefault(m => m.SemiProductCode == semiProductCode && m.RawMaterialCode == rawMaterialCode);

                                if (existingMaterial != null)
                                {
                                    // 更新现有记录
                                    existingMaterial.SemiProductDesc = semiProductDesc;
                                    existingMaterial.RawMaterialDesc = rawMaterialDesc;
                                    existingMaterial.Quantity = quantity;
                                    existingMaterial.UpdatedAt = DateTime.Now;
                                    duplicateCount++;
                                }
                                else
                                {
                                    // 添加新记录
                                    var newMaterial = new SemiProductMaterial
                                    {
                                        SemiProductCode = semiProductCode,
                                        SemiProductDesc = semiProductDesc,
                                        RawMaterialCode = rawMaterialCode,
                                        RawMaterialDesc = rawMaterialDesc,
                                        Quantity = quantity,
                                        CreatedAt = DateTime.Now,
                                        UpdatedAt = DateTime.Now
                                    };
                                    _dbContext.SemiProductMaterials.Add(newMaterial);
                                    importedCount++;
                                }
                            }
                            else
                            {
                                failedCount++;
                                errorLogs.Add($"列数不足，需要至少5列数据");
                            }
                        }
                        catch (Exception ex)
                        {
                            failedCount++;
                            errorLogs.Add($"处理失败: {ex.Message}");
                        }
                        finally
                        {
                            lineNumber++;
                        }
                    }

                    await _dbContext.SaveChangesAsync();
                }

                return Ok(new {
                    Message = "物料导入成功",
                    ImportedCount = importedCount,
                    DuplicateCount = duplicateCount,
                    FailedCount = failedCount,
                    ErrorLogs = errorLogs
                });
            }
            catch (Exception ex)
            {
                return BadRequest(new { Message = "导入失败", Error = ex.Message });
            }
        }
    }
}
