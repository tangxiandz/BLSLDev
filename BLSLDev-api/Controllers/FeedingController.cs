using BLSLDev_api.Data;
using BLSLDev_api.Models;
using Microsoft.AspNetCore.Mvc;

namespace BLSLDev_api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class FeedingController : ControllerBase
    {
        private readonly BLSLDbContext _dbContext;

        public FeedingController(BLSLDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        // 获取所有桶料列表
        [HttpGet("buckets")]
        public IActionResult GetAllBuckets()
        {
            var buckets = _dbContext.Buckets
                .Select(b => new {
                    b.Id,
                    b.Code,
                    b.RawMaterialCode,
                    b.RawMaterialDesc,
                    b.Weight,
                    b.Status
                })
                .ToList();

            return Ok(buckets);
        }

        // 获取物料对应的桶料号
        [HttpGet("buckets/{rawMaterialCode}")]
        public IActionResult GetBucketsByRawMaterial(string rawMaterialCode)
        {
            var buckets = _dbContext.Buckets
                .Where(b => b.RawMaterialCode == rawMaterialCode)
                .Select(b => b.Code)
                .ToList();

            return Ok(buckets);
        }

        // 添加桶料
        [HttpPost("bucket")]
        public IActionResult AddBucket([FromBody] Bucket bucket)
        {
            if (bucket == null)
            {
                return BadRequest("Invalid request body");
            }

            bucket.CreatedAt = DateTime.Now;
            bucket.UpdatedAt = DateTime.Now;
            
            _dbContext.Buckets.Add(bucket);
            _dbContext.SaveChanges();

            return Ok(new { Message = "桶料添加成功", Bucket = bucket });
        }

        // 更新桶料
        [HttpPut("bucket/{id}")]
        public IActionResult UpdateBucket(int id, [FromBody] Bucket bucket)
        {
            if (bucket == null)
            {
                return BadRequest("Invalid request body");
            }

            var existingBucket = _dbContext.Buckets.Find(id);
            if (existingBucket == null)
            {
                return NotFound("桶料不存在");
            }

            existingBucket.Code = bucket.Code;
            existingBucket.RawMaterialCode = bucket.RawMaterialCode;
            existingBucket.RawMaterialDesc = bucket.RawMaterialDesc;
            existingBucket.Weight = bucket.Weight;
            existingBucket.Status = bucket.Status;
            existingBucket.UpdatedAt = DateTime.Now;

            _dbContext.SaveChanges();

            return Ok(new { Message = "桶料更新成功", Bucket = existingBucket });
        }

        // 删除桶料
        [HttpDelete("bucket/{id}")]
        public IActionResult DeleteBucket(int id)
        {
            var bucket = _dbContext.Buckets.Find(id);
            if (bucket == null)
            {
                return NotFound("桶料不存在");
            }

            _dbContext.Buckets.Remove(bucket);
            _dbContext.SaveChanges();

            return Ok(new { Message = "桶料删除成功" });
        }

        // 验证桶料号并提交加料记录
        [HttpPost("validate")]
        public IActionResult ValidateBucket([FromBody] FeedingValidationRequest request)
        {
            if (request == null)
            {
                return BadRequest("Invalid request body");
            }

            // 获取物料对应的桶料号列表
            var validBuckets = _dbContext.Buckets
                .Where(b => b.RawMaterialCode == request.RawMaterialCode)
                .Select(b => b.Code)
                .ToList();

            // 验证桶料号是否在列表中
            bool isValid = validBuckets.Contains(request.BucketCode);
            string validationResult = isValid ? "验证成功" : "验证失败";

            // 保存加料记录
            var feedingRecord = new FeedingRecord
            {
                RawMaterialCode = request.RawMaterialCode,
                BucketCode = request.BucketCode,
                ValidationResult = validationResult,
                OperationTime = DateTime.Now
            };

            _dbContext.FeedingRecords.Add(feedingRecord);
            _dbContext.SaveChanges();

            return Ok(new {
                IsValid = isValid,
                Message = validationResult
            });
        }

        // 获取所有加料记录
        [HttpGet("records")]
        public IActionResult GetFeedingRecords()
        {
            var records = _dbContext.FeedingRecords
                .OrderByDescending(r => r.OperationTime)
                .Select(r => new {
                    r.Id,
                    r.RawMaterialCode,
                    r.BucketCode,
                    r.ValidationResult,
                    OperationTime = r.OperationTime.ToString("yyyy-MM-dd HH:mm:ss")
                })
                .ToList();

            return Ok(records);
        }

        // 导入桶料
        [HttpPost("import/buckets")]
        public async Task<IActionResult> ImportBuckets(IFormFile file)
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
                    await stream.ReadLineAsync();

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
                                var code = parts[0].Trim();
                                var rawMaterialCode = parts[1].Trim();
                                var rawMaterialDesc = parts[2].Trim();
                                var weight = decimal.TryParse(parts[3].Trim(), out var weightValue) ? weightValue : 0;
                                var status = parts[4].Trim();

                                // 检查是否已存在相同的桶号
                                var existingBucket = _dbContext.Buckets
                                    .FirstOrDefault(b => b.Code == code);

                                if (existingBucket != null)
                                {
                                    // 更新现有记录
                                    existingBucket.RawMaterialCode = rawMaterialCode;
                                    existingBucket.RawMaterialDesc = rawMaterialDesc;
                                    existingBucket.Weight = weight;
                                    existingBucket.Status = status;
                                    existingBucket.UpdatedAt = DateTime.Now;
                                    duplicateCount++;
                                }
                                else
                                {
                                    // 添加新记录
                                    var newBucket = new Bucket
                                    {
                                        Code = code,
                                        RawMaterialCode = rawMaterialCode,
                                        RawMaterialDesc = rawMaterialDesc,
                                        Weight = weight,
                                        Status = status,
                                        CreatedAt = DateTime.Now,
                                        UpdatedAt = DateTime.Now
                                    };
                                    _dbContext.Buckets.Add(newBucket);
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
                    Message = "桶料导入成功",
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

    public class FeedingValidationRequest
    {
        public required string RawMaterialCode { get; set; }
        public required string BucketCode { get; set; }
    }
}
