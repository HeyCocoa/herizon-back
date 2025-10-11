export const mbtiDimensionPairs = [
  { letters: ['E', 'I'] },
  { letters: ['S', 'N'] },
  { letters: ['T', 'F'] },
  { letters: ['J', 'P'] }
];

export const mbtiProfiles = {
  INTJ: {
    label: 'INTJ · 策略规划者',
    lines: [
      '喜欢拉远视角,提前规划路径。',
      '面对复杂议题会先独立想透。',
      '偏好高效、有结构的协作方式。',
      '记得多分享过程,倾听团队想法。'
    ]
  },
  INTP: {
    label: 'INTP · 逻辑探索者',
    lines: [
      '乐于拆解原理,反复验证假设。',
      '享受安静的思考空间与弹性节奏。',
      '问题导向,容易提出新点子。',
      '提醒自己合上脑洞,及时落地成果。'
    ]
  },
  ENTJ: {
    label: 'ENTJ · 目标指挥官',
    lines: [
      '看到目标就想统筹资源冲刺。',
      '决策干脆,执行节奏紧凑。',
      '善于分工与制定标准。',
      '别忘记照顾团队节奏与情绪。'
    ]
  },
  ENTP: {
    label: 'ENTP · 灵感推手',
    lines: [
      '话题跳跃快,擅长激发讨论。',
      '喜欢尝试新点子与跨界组合。',
      '对变化反应快,执行易转弯。',
      '完成度靠提醒自己聚焦重点。'
    ]
  },
  INFJ: {
    label: 'INFJ · 愿景引导者',
    lines: [
      '关注意义与长期影响。',
      '善于洞察人心与需求。',
      '擅长把愿景翻译成行动线。',
      '适度表达边界,避免过度承担。'
    ]
  },
  INFP: {
    label: 'INFP · 故事织梦者',
    lines: [
      '价值观驱动,做事要对得起内心。',
      '擅长写作、故事化表达与共情。',
      '喜欢慢慢打磨灵感与作品。',
      '为自己设定节点,帮助落地。'
    ]
  },
  ENFJ: {
    label: 'ENFJ · 团队教练',
    lines: [
      '敏锐地照顾到团队氛围。',
      '擅长激励、协调与资源连接。',
      '做事讲求仪式感与整体一致。',
      '也要留时间照顾自己的感受。'
    ]
  },
  ENFP: {
    label: 'ENFP · 活力连接者',
    lines: [
      '热衷认识人和探索新鲜机会。',
      '灵感多,擅长把点子串成故事。',
      '行动自由,乐于随境调节。',
      '列个清单,帮自己收束能量。'
    ]
  },
  ISTJ: {
    label: 'ISTJ · 秩序守护者',
    lines: [
      '喜欢明确流程与责任划分。',
      '做事细致稳健,按部就班。',
      '擅长发现隐患并及时修正。',
      '试着多分享想法,拥抱新变化。'
    ]
  },
  ISFJ: {
    label: 'ISFJ · 体贴照护者',
    lines: [
      '细心体贴,愿意支援伙伴。',
      '记忆力好,能照顾到细节。',
      '喜欢在熟悉的框架中前进。',
      '表达个人需求,让能量更稳定。'
    ]
  },
  ESTJ: {
    label: 'ESTJ · 执行管理者',
    lines: [
      '讲究效率与成果,行动力强。',
      '善于制定计划与规范。',
      '遇到问题会亲自上阵解决。',
      '练习倾听不同意见,避免独断。'
    ]
  },
  ESFJ: {
    label: 'ESFJ · 氛围协调者',
    lines: [
      '关注大家的体验与节奏。',
      '擅长统筹活动与分配资源。',
      '喜欢在合作中建立信任感。',
      '照顾别人同时也记得休息。'
    ]
  },
  ISTP: {
    label: 'ISTP · 动手解决者',
    lines: [
      '冷静务实,喜欢亲手实验。',
      '面对突发状况反应迅速。',
      '享受探索工具与技巧。',
      '保持沟通,让伙伴跟上节奏。'
    ]
  },
  ISFP: {
    label: 'ISFP · 体验创造者',
    lines: [
      '感受力丰富,热爱手作与美感。',
      '做事讲究当下氛围与体验。',
      '乐于按自己的步调前进。',
      '适度规划,帮创意照进现实。'
    ]
  },
  ESTP: {
    label: 'ESTP · 行动派探索者',
    lines: [
      '擅长现场反应与快速试错。',
      '喜欢挑战与带动气氛。',
      '偏爱边做边学的效率感。',
      '偶尔停下来评估风险与后续。'
    ]
  },
  ESFP: {
    label: 'ESFP · 现场带动者',
    lines: [
      '社交能量足,善于感染他人。',
      '享受体验生活的每个瞬间。',
      '喜欢即兴发挥与互动。',
      '制定小目标,帮助保持节奏。'
    ]
  }
};

export function calculateMbtiResult(answers = []) {
  if (!Array.isArray(answers) || answers.length === 0) {
    return null;
  }

  const counts = { E: 0, I: 0, S: 0, N: 0, T: 0, F: 0, J: 0, P: 0 };

  answers.forEach((value) => {
    if (counts[value] !== undefined) {
      counts[value] += 1;
    }
  });

  const type = mbtiDimensionPairs
    .map(({ letters: [first, second] }) => {
      const firstCount = counts[first] || 0;
      const secondCount = counts[second] || 0;
      return firstCount >= secondCount ? first : second;
    })
    .join('');

  return {
    type,
    profile: mbtiProfiles[type] || null,
    counts
  };
}
